package com.predictionalgorithm

import javax.swing.SwingUtilities
import com.predictionalgorithm.controlinterface._
import com.predictionalgorithm.prediction.KNNPrediction
import com.predictionalgorithm.serverui.MonitoringUI


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
