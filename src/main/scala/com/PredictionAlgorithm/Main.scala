package com.PredictionAlgorithm

import java.io.File
import javax.swing.{SwingUtilities, JFrame}
import akka.actor.{Props, ActorSystem}
import akka.io.IO
import akka.util.Timeout
import com.PredictionAlgorithm.ControlInterface._
import com.PredictionAlgorithm.DataDefinitions.TFL.{TFLDefinitions, LoadStopDefinitions}
import com.PredictionAlgorithm.Spray.{MyServiceActor, Boot}
import com.PredictionAlgorithm.UI.{MonitoringUI}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._


object Main extends App {

  val UI_REFRESH_INTERVAL:Int = 1000;


  SwingUtilities.invokeLater(new Runnable() {
    def run {
      val ui = new MonitoringUI(UI_REFRESH_INTERVAL)
      ui.setStreamProcessing(StreamProcessingControlInterface)
      ui.setHistoricalDataCollection(HistoricalDataCollectionControlInterface)
      ui.setLiveStreaming(LiveStreamControlInterface)
      ui.setQueryProcessing(new QueryController)//TODO remove class and replace with obect
      ui.setUpdateRouteDefinitions(UpdateRouteDefinitionsControlInterface)
      ui.setUpdateStopDefinitions(UpdateStopDefinitionsControlInterface)
      ui.setAddPolyLines(AddPolyLinesControlInterface)
      ui.setCleanUpPointToPoint(CleanUpPointToPointControlInterface)
      ui.createAndDisplayGUI

    }
  })


  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "routingActor")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
 // IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = 8080)
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)


  //println(RoutePredictionMapping.getRoutePredictionMap("3",1,"THU",75600))

}
