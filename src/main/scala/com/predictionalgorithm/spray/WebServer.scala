package com.predictionalgorithm.spray

import java.io.ByteArrayInputStream

import akka.actor._
import com.predictionalgorithm.commons.Commons
import com.predictionalgorithm.commons.Commons._
import com.predictionalgorithm.datadefinitions.tfl.TFLDefinitions
import com.predictionalgorithm.prediction.{KNNPredictionImpl, PredictionRequest}
import com.predictionalgorithm.streaming.PackagedStreamObject
import spray.can.websocket.FrameCommandFailed
import spray.can.websocket.frame.{TextFrameStream, TextFrame}
import spray.can.{websocket, Http}
import spray.http.HttpRequest
import spray.routing.HttpServiceActor
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

/**
 * This code is adapted from an example from https://github.com/wandoulabs/spray-websocket
 */
object WebServer {

  final case class Push(routeID: String, latitude:Double, longitude:Double, msg: String)
  final case class PushToChildren(pso: PackagedStreamObject)

  object WebSocketServer {
    def props() = Props(classOf[WebSocketServer])
  }

  class WebSocketServer extends Actor with ActorLogging {
    def receive = {
      /**
       * Sets up a new actor for each new connection
       */
      case Http.Connected(remoteAddress, localAddress) =>
        val serverConnection = sender()
        val conn = context.actorOf(WebSocketWorker.props(serverConnection))
        serverConnection ! Http.Register(conn)

      /**
       * Pushes each new position update to the children connected (as an JSON encoded object)
       */
      case PushToChildren(pso: PackagedStreamObject) =>
        val children = context.children
        val encoded = encodePackageObject(pso)
        val firstLat = if (!pso.markerMovementData.isEmpty) pso.markerMovementData(0)._1.toDouble else 0
        val firstLng = if (!pso.markerMovementData.isEmpty) pso.markerMovementData(0)._2.toDouble else 0
        children.foreach(ref => ref ! Push(pso.route_ID, firstLat, firstLng, encoded))

    }
  }

  object WebSocketWorker {
    def props(serverConnection: ActorRef) = Props(classOf[WebSocketWorker], serverConnection)
  }

  class WebSocketWorker(val serverConnection: ActorRef) extends HttpServiceActor with websocket.WebSocketServerWorker {

    // Escalates to web socket
    override def receive = handshaking orElse businessLogicNoUpgrade orElse closeLogic

    var mode = "NONE"
    var selectedRadius = 0.0
    var routeList: List[String] = List()
    var centrePoint:Array[Double] = Array()

    def businessLogic: Receive = {

      case x@(_: TextFrame) =>
        val receivedStr = x.payload.utf8String
        if (receivedStr.startsWith("ROUTELIST")) {
          mode = "ROUTELIST"
          val splitReceive = receivedStr.split(",").drop(1).toList
          routeList = routeList ++ splitReceive
          println("1 connection: " + routeList)
        } else if (receivedStr.startsWith("RADIUS")) {
          mode = "RADIUS"
          val temporaryStr = receivedStr.replaceAll("\\)","").replaceAll("\\(","").split(",").drop(1).map(_.toDouble) //Take out brackets
          selectedRadius = temporaryStr.head //Sets the radius
          centrePoint = temporaryStr.drop(1) // Sets the centre Point
        }

      case Push(routeID: String, latitude: Double, longitude: Double, message: String) =>

        if (mode == "ROUTELIST") {
          // Sends a text frame to the clients that are listening to that particular route
          if (routeList.contains(routeID)) {
            send(TextFrame(message))
          }
        } else if (mode == "RADIUS") {
          val centreLat = centrePoint(0)
          val centreLng = centrePoint(1)

          if (Commons.getDistance(centreLat,centreLng,latitude,longitude) < selectedRadius) {
            send(TextFrame(message))
          }
        }

      case x: FrameCommandFailed =>
        log.error("frame command failed", x)

      case x: HttpRequest => //Log this


    }

    def businessLogicNoUpgrade: Receive = {
      runRoute(thisRoute)
    }

    val thisRoute = {

      pathPrefix("css") {
        get {
          getFromResourceDirectory("css")
        }
      } ~
        pathPrefix("keystore") {
          get {
            getFromResourceDirectory("keystore")
          }
        } ~
        pathPrefix("js") {
          get {
            getFromResourceDirectory("js")
          }
        } ~
        pathPrefix("images") {
          get {
            getFromResourceDirectory("images")
          }
        } ~
        path("favicon.ico") {
          getFromResource("images/favicon.ico")
        } ~
        path("map") {
          getFromResource("html/livemap.html")
        } ~
        path("prediction") {
          getFromResource("html/prediction.html")
        } ~
        path("route_list_request.asp") {
          get {
            complete {
              getRouteList
            }
          }
        } ~
        path("direction_list_request.asp") {
          get {
            parameters("route") { route =>
              complete {
                getDirectionList(route)
              }
            }
          }
        } ~
        path("stop_list_request.asp") {
          get {
            parameters("route") { (route) =>
              parameters("direction") { (direction) =>
                complete {
                  getStopList(route, direction.toInt)
                }
              }
            }
          }
        } ~
        path("prediction_request.asp") {
          get {
            parameters("route") { (route) =>
              parameters("direction") { (direction) =>
                parameters("fromStop") { (fromStop) =>
                  parameters("toStop") { (toStop) =>
                    println(route + "," + direction + "," + fromStop + "," + toStop)
                    complete {
                      makePrediction(route, direction.toInt, fromStop, toStop)
                    }
                  }
                }
              }
            }
          }
        }
    }
  }


  private def getRouteList: String = {
    val routeList: List[String] = TFLDefinitions.RouteDefinitionMap.map(x => x._1._1).toSet.toList.sorted
    val jsonMap = Map("routeList" -> routeList)
    compact(render(jsonMap))
  }

  private def getDirectionList(routeID: String): String = {
    val outwardDirection = TFLDefinitions.StopDefinitions(TFLDefinitions.RouteDefinitionMap.get(routeID, 1).get.last._2).stopPointName
    val returnDirection = TFLDefinitions.StopDefinitions(TFLDefinitions.RouteDefinitionMap.get(routeID, 2).get.last._2).stopPointName
    val jsonMap = Map("directionList" -> List("1," + outwardDirection, "2," + returnDirection))
    compact(render(jsonMap))
  }

  private def getStopList(routeID: String, directionID: Int): String = {
    val stopList = TFLDefinitions.RouteDefinitionMap(routeID, directionID).map(x => x._2 + "," + TFLDefinitions.StopDefinitions(x._2).stopPointName)
    val jsonMap = Map("stopList" -> stopList)
    compact(render(jsonMap))
  }

  private def makePrediction(routeID: String, directionID: Int, fromStop: String, toStop: String): String = {
    val pr = new PredictionRequest(routeID, directionID, fromStop, toStop, System.currentTimeMillis().getDayCode, System.currentTimeMillis().getTimeOffset)
    val prediction = KNNPredictionImpl.makePrediction(pr)
    if (prediction.isDefined) prediction.get._1.toString + "," + prediction.get._2.toString else "Unable to make a prediction at this time"
  }


  /**
   * Encodes a package of live bus movements to JSON
   * @param next The next object to be encoded
   * @return A string in JSON format
   */
  private def encodePackageObject(next: PackagedStreamObject): String = {
    val streamFields = Array("reg", "nextArr", "movementData", "routeID", "directionID", "towards", "nextStopID", "nextStopName")

    val nextList = Map(
      streamFields(0) -> next.reg,
      streamFields(1) -> next.nextArrivalTime,
      streamFields(2) -> compact(render(next.markerMovementData.map({ case (lat, lng, rot, propDis) => lat + "," + lng + "," + rot + "," + propDis }).toList)),
      streamFields(3) -> next.route_ID,
      streamFields(4) -> next.direction_ID.toString,
      streamFields(5) -> next.towards,
      streamFields(6) -> next.nextStopID,
      streamFields(7) -> next.nextStopName)
    //val json = compact(render(nextList))
    compact(render(nextList))
    // val nextChunk = MessageChunk("data: " + json + "\n\n")}
  }
}
