package com.PredictionAlgorithm.Spray


import akka.actor.{ActorSystem, ActorLogging, Props, Actor}
import akka.io.Tcp
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.ControlInterface.{LiveStreamControlInterface, QueryController}
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Streaming.{LiveStreamResult, LiveStreamingCoordinator}
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

  val sc = LiveStreamControlInterface
  val stream: Iterator[(String, Double, Double)] = sc.getStream
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
      path("maps") {
        getFromResource("html/mapstest.html")
      } ~
    path("predict") {
      get {
          complete {
            <html>
              <link rel="stylesheet" href="css/form.css"/>
              <body>
                <form method="post">
                  <p>
                    <label for="a">Route:</label>
                    <input type="text" name="route_ID"></input>
                  </p>
                  <p>
                    <label for="a">Direction:</label>
                    <input type="text" name="direction_ID"></input>
                  </p>
                  <p>
                    <label for="a">From ID:</label>
                    <input type="text" name="from_ID"></input>
                  </p>
                  <p>
                    <label for="a">To ID:</label>
                    <input type="text" name="to_ID"></input>
                  </p>
                  <p>
                    <label for="a">Day Code:</label>
                    <input type="text" name="day_code"></input>
                  </p>
                  <p>
                    <input type="submit" value="Submit"></input>
                  </p>
                </form>
              </body>
            </html>
        }
      } ~
        post {
          formFields('route_ID, 'direction_ID, 'from_ID, 'to_ID, 'day_code) { (route: String, dir: String, from: String, to: String, day: String) => {
            val result = new QueryController().makePrediction(route, dir.toInt, from, to, day, Commons.getTimeOffset(System.currentTimeMillis))
            complete(<h1>Prediction:
              {result}
            </h1>)
          }
          }

        }
      } ~
      path("stream") {
        respondAsEventStream {
          sendSSE
        }
      }
  }

  case class Ok()

  // This streaming method has been adapted from a demo at https://github.com/chesterxgchen/sse-demo
  def sendSSE(ctx: RequestContext): Unit = {
    actorRefFactory.actorOf {
      Props {
        new Actor {
          // we use the successful sending of a chunk as trigger for scheduling the next chunk
          val responseStart = HttpResponse(entity = HttpEntity(`text/event-stream`, "data: start\n\n"))
          ctx.responder ! ChunkedResponseStart(responseStart).withAck(Ok)

          def receive = {
            case Ok =>
             // in(Duration(500, MILLISECONDS))
              {
                val next = stream.next()
                val nextList = Map("reg" -> next._1,
               //   "route" -> next._2.routeID,
                //  "dir" -> next._2.directionID.toString,
                //  "point" -> next._2.nextPointSeq.toString,
                //  "stopCode" -> next._2.nextStopCode,
                //  "stopName" -> next._2.nextStopName,
                  "lat" -> next._2.toString,
                  "lng" -> next._3.toString)
                 // "arrivalTime" -> next._2.arrivalTimeStamp.toString)
                val json = compact(render(nextList))

                val nextChunk = MessageChunk("data: " + json +"\n\n")
                ctx.responder ! nextChunk.withAck(Ok)
              }

            case ev: Tcp.ConnectionClosed => //
          }
        }
      }
    }
  }

  def respondAsEventStream =
    respondWithHeader(`Cache-Control`(`no-cache`)) &
      respondWithHeader(`Connection`("Keep-Alive")) &
      respondWithMediaType(`text/event-stream`)

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    ActorSystem().scheduler.scheduleOnce(duration)(body)

}

