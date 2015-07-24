package com.PredictionAlgorithm

import java.io.File
import javax.swing.{SwingUtilities, JFrame}
import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.ControlInterface.{StreamController, QueryController, DataReadProcessStoreControlInterface}
import com.PredictionAlgorithm.DataDefinitions.TFL.{TFLDefinitions, LoadStopDefinitionsFromWeb}
import com.PredictionAlgorithm.Prediction.RoutePredictionMapping
import com.PredictionAlgorithm.Spray.Boot
import com.PredictionAlgorithm.UI.{MonitoringUI}

/**
 * Created by chrischivers on 21/06/15.
 */
object Main extends App {

  val UI_REFRESH_INTERVAL:Int = 1000;

  SwingUtilities.invokeLater(new Runnable() {
    def run {
      val ui = new MonitoringUI(UI_REFRESH_INTERVAL)
      ui.setDataSourceProcess(DataReadProcessStoreControlInterface)
      ui.setQueryProcessing(new QueryController)//TODO remove class and replace with obect
      ui.setStreamProcessing(new StreamController)
      ui.createAndDisplayGUI

    }
  })

  //println(RoutePredictionMapping.getRoutePredictionMap("3",1,"THU",75600))

}
