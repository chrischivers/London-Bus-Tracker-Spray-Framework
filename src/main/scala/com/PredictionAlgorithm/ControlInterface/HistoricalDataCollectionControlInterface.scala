package com.PredictionAlgorithm.ControlInterface

import com.PredictionAlgorithm.Database.TFL.TFLInsertPointToPointDuration
import com.PredictionAlgorithm.Processes.TFL.TFLProcessSourceLines

/**
 * User Control Interface for the Historical Data Collection
 */
object HistoricalDataCollectionControlInterface extends StartStopControlInterface {


  override def start(): Unit = {
TFLProcessSourceLines.setHistoricalDataStoring(true)

  }

  override def stop(): Unit = {
    TFLProcessSourceLines.setHistoricalDataStoring(false)
  }

  override def getVariableArray: Array[String] = {
    val numberInHoldingBuffer = TFLProcessSourceLines.getBufferSize.toString
    val numberNonMatches = TFLProcessSourceLines.numberNonMatches.toString
    val numberDBTransactionsRequested = TFLInsertPointToPointDuration.numberDBTransactionsRequested.toString
    val numberDBTransactionsExecuted = TFLInsertPointToPointDuration.numberDBTransactionsExecuted.toString
    val numberDBTransactionsOutstanding = (TFLInsertPointToPointDuration.numberDBTransactionsRequested - TFLInsertPointToPointDuration.numberDBTransactionsExecuted).toString
    val numberDBPullTransactionsExecuted = TFLInsertPointToPointDuration.numberDBPullTransactionsExecuted.toString
    Array(numberInHoldingBuffer, numberNonMatches, numberDBTransactionsRequested, numberDBTransactionsExecuted, numberDBTransactionsOutstanding, numberDBPullTransactionsExecuted)
  }


}
