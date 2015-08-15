package com.PredictionAlgorithm.Spray

import akka.actor._
import akka.io.IO
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.ControlInterface.QueryController
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Streaming.PackagedStreamObject
import org.json4s.native.JsonMethods._
import spray.can.server.UHttp
import spray.can.websocket.FrameCommandFailed
import spray.can.websocket.frame.TextFrame
import spray.can.{websocket, Http}
import spray.http.HttpRequest
import spray.routing.HttpServiceActor
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._


object SimpleServer extends MySslConfiguration {
  //This code is based on example from https://github.com/wandoulabs/spray-websocket

  final case class Push(routeID: String, msg: String)

  final case class PushToChildren(pso: PackagedStreamObject)

  object WebSocketServer {
    def props() = Props(classOf[WebSocketServer])
  }

  class WebSocketServer extends Actor with ActorLogging {
    def receive = {
      // when a new connection comes in we register a WebSocketConnection actor as the per connection handler
      case Http.Connected(remoteAddress, localAddress) =>
        val serverConnection = sender()
        val conn = context.actorOf(WebSocketWorker.props(serverConnection))
        serverConnection ! Http.Register(conn)
      case PushToChildren(pso: PackagedStreamObject) =>
        val children = context.children
        val encoded = encodePackageObject(pso)
        children.foreach(ref => ref ! Push(pso.route_ID, encoded))
    }
  }

  object WebSocketWorker {
    def props(serverConnection: ActorRef) = Props(classOf[WebSocketWorker], serverConnection)
  }

  class WebSocketWorker(val serverConnection: ActorRef) extends HttpServiceActor with websocket.WebSocketServerWorker {
    override def receive = handshaking orElse businessLogicNoUpgrade orElse closeLogic

    var routeList: List[String] = List()

    def businessLogic: Receive = {
      case x@(_: TextFrame) =>
        //sender() ! x
        val splitReceive =  x.payload.utf8String.split(",").toList
        routeList = routeList ++ splitReceive
        println(routeList)

      case Push(routeID:String, message:String) => {
        if (routeList.contains(routeID)) {
          send(TextFrame(message))
        }

      }

      case x: FrameCommandFailed =>
        log.error("frame command failed", x)

      case x: HttpRequest => // do something
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
        path("map") {
          getFromResource("html/livemap.html")
        } ~
        path("prediction") {
          getFromResource("html/prediction.html")
        } ~
        path("route_list_request.asp") {
          get {
            complete {
              sendRouteList
            }
          }
        }
    }

  }


  def sendRouteList: String = {
    val routeList: List[String] = TFLDefinitions.RouteDefinitionMap.map(x => x._1._1).toSet.toList.sorted
    val jsonMap = Map("routeList" -> routeList)
    compact(render(jsonMap))


  }


  def encodePackageObject(next: PackagedStreamObject): String = {
    val streamFields = Array("reg", "nextArr", "movementData", "routeID", "directionID", "towards", "nextStopID", "nextStopName")

    val nextList = Map(
      streamFields(0) -> next.reg,
      streamFields(1) -> next.nextArrivalTime,
      streamFields(2) -> compact(render(next.markerMovementData.map({ case (lat, lng, rot, propDist, labx, laby) => lat + "," + lng + "," + rot + "," + propDist + "," + labx + "," + laby }).toList)),
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
