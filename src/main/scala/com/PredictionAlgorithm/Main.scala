package com.PredictionAlgorithm

import javax.swing.SwingUtilities
import com.PredictionAlgorithm.ControlInterface._
import com.PredictionAlgorithm.Prediction.KNNPrediction
import com.PredictionAlgorithm.UI.MonitoringUI


object Main extends App {

  /**
   * How frequently the server UI refreshes
   */
  val UI_REFRESH_INTERVAL:Int = 1000


  /**
   * Starts the Server UI using Swing Framework
   */
  SwingUtilities.invokeLater(new Runnable() {
    def run {
      val ui = new MonitoringUI(UI_REFRESH_INTERVAL)
      ui.setStreamProcessing(StreamProcessingControlInterface)
      ui.setHistoricalDataCollection(HistoricalDataCollectionControlInterface)
      ui.setLiveStreaming(LiveStreamControlInterface)
      ui.setQueryProcessing(KNNPrediction)
      ui.setUpdateRouteDefinitions(UpdateRouteDefinitionsControlInterface)
      ui.setUpdateStopDefinitions(UpdateStopDefinitionsControlInterface)
      ui.setAddPolyLines(AddPolyLinesControlInterface)
      ui.setCleanUpPointToPoint(CleanUpPointToPointControlInterface)
      ui.createAndDisplayGUI()

    }
  })

}
