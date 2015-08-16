package com.PredictionAlgorithm

import java.io.File
import javax.swing.{SwingUtilities, JFrame}
import akka.actor.{Props, ActorSystem}
import akka.io.IO
import akka.util.Timeout
import com.PredictionAlgorithm.ControlInterface._
import com.PredictionAlgorithm.DataDefinitions.TFL.{TFLDefinitions, LoadStopDefinitions}
import com.PredictionAlgorithm.Spray.SimpleServer.WebSocketServer
import com.PredictionAlgorithm.Spray.{SimpleServer}
import com.PredictionAlgorithm.UI.{MonitoringUI}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import spray.can.server.UHttp
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
      ui.createAndDisplayGUI()

    }
  })

}
