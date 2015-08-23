package com.PredictionAlgorithm.ControlInterface

import com.PredictionAlgorithm.DataDefinitions.Tools.CleanPointToPointData

/**
 * User Control Interface for the adding of Clean Up PointToPoint Function
 */
object CleanUpPointToPointControlInterface extends StartStopControlInterface {

  override def getVariableArray: Array[String] = {
    val numberDocumentsRead = CleanPointToPointData.numberDocumentsRead
    val numberDocumentsDeleted = CleanPointToPointData.numberDocumentsDeleted
    Array(numberDocumentsRead.toString, numberDocumentsDeleted.toString)
  }

  override def stop(): Unit = CleanPointToPointData.stop()

  override def start(): Unit = CleanPointToPointData.start()
}


