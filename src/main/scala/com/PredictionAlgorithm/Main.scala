package com.PredictionAlgorithm

import java.io.File
import javax.swing.{SwingUtilities, JFrame}
import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.ControlInterface.DataSourceControlInterface
import com.PredictionAlgorithm.UI.{MonitoringUI}

/**
 * Created by chrischivers on 21/06/15.
 */
object Main extends App {

  val UI_REFRESH_INTERVAL:Int = 1000;

  SwingUtilities.invokeLater(new Runnable() {
    def run {
      val ui = new MonitoringUI(UI_REFRESH_INTERVAL)
      ui.setDataSourceProcess(DataSourceControlInterface)
      ui.createAndDisplayGUI

    }
  })

}
