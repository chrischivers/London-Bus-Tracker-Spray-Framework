package com.PredictionAlgorithm.Spray


import akka.actor.Actor
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.ControlInterface.QueryController
import spray.routing._
import spray.http._
import MediaTypes._

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

  val thisRoute = {
    path("predict") {

      get{
        respondWithMediaType(`text/html`) {
          // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <form method="post">
                  <br>Route:
                    <input type="text" name="route_ID"></input>
                  </br>
                  <br>Direction:
                    <input type="text" name="direction_ID"></input>
                  </br>
                  <br>From ID:
                    <input type="text" name="from_ID"></input>
                  </br>
                  <br>To ID:
                    <input type="text" name="to_ID"></input>
                  </br>
                  <br>Day Code:
                    <input type="text" name="day_code"></input>
                  </br>
                  <input type="submit" value="Submit"></input>
                </form>
              </body>
            </html>


          }

        }
      } ~
        post {
          formFields('route_ID, 'direction_ID, 'from_ID, 'to_ID, 'day_code) { (route:String, dir:String, from:String, to:String, day:String) =>
          {
            val result = new QueryController().makePrediction(route, dir.toInt, from, to, day, Commons.getTimeOffset(System.currentTimeMillis))
            complete(<h1>Prediction {result} Created</h1>)
          }
        }
        }
    }
  }

}


    /*path("predict") {


      }


    } ~

    path ("formresponse") {

      post {

        }

      }
    }*/
