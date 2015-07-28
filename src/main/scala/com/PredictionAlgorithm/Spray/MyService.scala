package com.PredictionAlgorithm.Spray


import akka.actor.{ActorSystem, ActorLogging, Props, Actor}
import akka.io.Tcp
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.ControlInterface.{StreamController, QueryController}
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Streaming.LiveStreamingCoordinator
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

  val sc = new StreamController
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
      path("getPosition.asp") {
          post {
            entity(as[String]) { returned => {
              val routeID = "3"
              val directionID = 1
              try {
                val json = compact(render(sc.getPositionSnapshotsForRoute(routeID).map(x => {
                  Map("reg" -> x._1,
                    "route" -> x._2.routeID,
                  "dir" -> x._2.directionID.toString,
                  "point" -> x._2.nextPointSeq.toString,
                  "stopCode" -> x._2.nextStopCode,
                  "stopName" -> x._2.nextStopName,
                  "lat" -> x._2.nextStopLat.toString,
                  "lng" -> x._2.nextStopLng.toString,
                  "arrivalTime" -> x._2.arrivalTimeStamp.toString)
                }).toList))
                println(json)
                complete(json)
                /*val stopCode = sc.getCurrentPosition.nextStopCode
                println("TIME TILL NEXT STOP: " + sc.getCurrentPosition.timeTilNextStop)
                val result = TFLDefinitions.StopDefinitions(stopCode)
                val conString = stopCode + "," + result.latitude + "," + result.longitude + "," + sc.getCurrentPosition.timeTilNextStop
                complete({
                  conString
                })*/
              } catch {
                case e: InstantiationError => {
                  println("Error: cannot get route: " + e)
                  complete({
                    "NA"
                  })
                }
              }
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

  // Some Streaming Code taken from Demo on GitHub:
  //https://github.com/spray/spray/blob/release/1.1/examples/spray-routing/on-spray-can/src/main/scala/spray/examples/DemoService.scala

  // we prepend 2048 "empty" bytes to push the browser to immediately start displaying the incoming chunks
  lazy val streamStart = " " * 2048 + "<html><body><h2>A streaming response</h2><p>(for 15 seconds)<ul>"
  lazy val streamEnd = "</ul><p>Finished.</p></body></html>"


  def stringStream: Stream[String] = {
    val secondStream = Stream.continually {
      // CAUTION: we block here to delay the stream generation for you to be able to follow it in your browser,
      // this is only done for the purpose of this demo, blocking in actor code should otherwise be avoided
      Thread.sleep(250)
      DateTime.now.toIsoDateTimeString
    }
    streamStart #:: secondStream #::: streamEnd #:: Stream.empty
  }

  case class Ok(remaining: Int)

  def sendSSE(ctx: RequestContext): Unit = {
    actorRefFactory.actorOf {
      Props {
        new Actor with ActorLogging {
          // we use the successful sending of a chunk as trigger for scheduling the next chunk
          val responseStart = HttpResponse(entity = HttpEntity(`text/event-stream`, "data: start\n\n"))
          log.info(" start chunk response  with 10 iterations")
          ctx.responder ! ChunkedResponseStart(responseStart).withAck(Ok(10))

          def receive = {
            case Ok(0) =>
              log.info(" going to stop it ")
              ctx.responder ! MessageChunk("data: " + 100 + "\n\n")
              ctx.responder ! MessageChunk("data: Finished.\n\n")
              ctx.responder ! ChunkedMessageEnd
              context.stop(self)
            case Ok(remaining) =>
              log.info(" got ok remaining " + remaining)
              in(Duration(500, MILLISECONDS)) {
                val nextChunk = MessageChunk("data: " + (10 - remaining) * 10 + "\n\n")
                ctx.responder ! nextChunk.withAck(Ok(remaining - 1))
              }

            case ev: Tcp.ConnectionClosed =>
              log.warning("Stopping response streaming due to {}", ev)
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

