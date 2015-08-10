package com.PredictionAlgorithm.Spray


import akka.actor.{ActorSystem, ActorLogging, Props, Actor}
import akka.io.Tcp
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.ControlInterface.{LiveStreamControlInterface, QueryController}
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Streaming.{PackagedStreamObject, LiveStreamResult, LiveStreamingCoordinator}
import spray.http.CacheDirectives.`no-cache`
import spray.http.HttpHeaders.`Cache-Control`
import spray.routing._
import spray.http._
import MediaTypes._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import spray.http.HttpHeaders.{`Content-Type`, Connection, `Cache-Control`}

import scala.concurrent.duration._


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = {
    //runRoute(myRoute)
    runRoute(thisRoute)
  }


}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  implicit def executionContext = actorRefFactory.dispatcher

  val streamFields = Array("reg","nextArr","latLng","routeID", "directionID", "towards","nextStopID","nextStopName")
  val `text/event-stream` = MediaType.custom("text/event-stream")
  MediaTypes.register(`text/event-stream`)


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
      path("maps") {
        getFromResource("html/mapstest.html")
      } ~
    path("prediction") {
            getFromResource("html/prediction.html")
      } ~
        post {
          formFields('route_ID, 'direction_ID, 'from_ID, 'to_ID, 'day_code) { (route: String, dir: String, from: String, to: String, day: String) => {
            val result = new QueryController().makePrediction(route, dir.toInt, from, to, day, Commons.getTimeOffset(System.currentTimeMillis))
            complete(<h1>Prediction:
              {result}
            </h1>)
          }
          }
      } ~
      path("stream") {
        respondAsEventStream {
          new sendStream().sendSSE
        }
      } ~
    path ("route_list_request.asp") {
      get {
        complete{
          sendRouteList
        }
      }
    }
  }

  class sendStream {

    case class Ok()

    // This streaming method has been adapted from a demo at https://github.com/chesterxgchen/sse-demo
    def sendSSE(ctx: RequestContext): Unit = {
      actorRefFactory.actorOf {
        Props {
          new Actor {
            val streamImpl: FIFOStreamImplementation = new FIFOStreamImplementation()
            LiveStreamingCoordinator.registerNewStream(streamImpl)
            val stream: Iterator[PackagedStreamObject] = streamImpl.toStream.iterator

            // we use the successful sending of a chunk as trigger for scheduling the next chunk
            val responseStart = HttpResponse(entity = HttpEntity(`text/event-stream`, "data: start\n\n"))
            ctx.responder ! ChunkedResponseStart(responseStart).withAck(Ok)

            def receive = {
              case Ok =>
                // in(Duration(500, MILLISECONDS)) {
                val next = stream.next()
                val nextList = Map(
                  streamFields(0) -> next.reg,
                  streamFields(1) -> next.nextArrivalTime,
                  streamFields(2) -> compact(render(next.decodedPolyLineToNextStop.map(x => x._1 + "," + x._2).toList)),
                  streamFields(3) -> next.route_ID,
                  streamFields(4) -> next.direction_ID.toString,
                  streamFields(5) -> next.towards,
                  streamFields(6) -> next.nextStopID,
                  streamFields(7) -> next.nextStopName)

                val json = compact(render(nextList))

                val nextChunk = MessageChunk("data: " + json + "\n\n")
                ctx.responder ! nextChunk.withAck(Ok)

              case ev: Tcp.ConnectionClosed => //
            }
          }
        }
      }
    }


    def in[U](duration: FiniteDuration)(body: => U): Unit =
      ActorSystem().scheduler.scheduleOnce(duration)(body)
  }

  def respondAsEventStream =
    respondWithHeader(`Cache-Control`(`no-cache`)) &
      respondWithHeader(`Connection`("Keep-Alive")) &
      respondWithMediaType(`text/event-stream`)

  def sendRouteList: String = {
    val routeList: List[String] = TFLDefinitions.RouteDefinitionMap.map(x=>x._1._1).toSet.toList.sorted
    val jsonMap = Map("routeList" -> routeList)
    compact(render(jsonMap))
  }

}

