package com.PredictionAlgorithm.Spray


import akka.actor.Actor
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.ControlInterface.{StreamController, QueryController}
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Streaming.LiveStreamingCoordinator
import spray.routing._
import spray.http._
import MediaTypes._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._


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

  val sc = new StreamController


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
        // we detach in order to move the blocking code inside the simpleStringStream into a future
        detach() {
          respondWithMediaType(`text/html`) { // normally Strings are rendered to text/plain, we simply override here
            complete(stringStream)
          }
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
      Thread.sleep(10)
      "<li>" + sc.getStream.next() + "</li>"
    }
    streamStart #:: secondStream #::: streamEnd #:: Stream.empty
  }



}
