package com.PredictionAlgorithm.ControlInterface

import akka.actor.Props
import com.PredictionAlgorithm.Database.TFL.TFLInsertPointToPointDuration
import com.PredictionAlgorithm.Processes.TFL.{TFLIterateOverArrivalStream, TFLProcessSourceLines}


object HistoricalDataCollectionControlInterface extends StartStopControlInterface {


  override def start: Unit = {
TFLProcessSourceLines.setHistoricalDataStoring(true)

  }

  override def stop: Unit = {
    TFLProcessSourceLines.setHistoricalDataStoring(false)
  }

  override def getVariableArray: Array[String] = {
    val numberInHoldingBuffer = TFLProcessSourceLines.getBufferSize.toString
    val numberDBTransactionsRequested = TFLInsertPointToPointDuration.numberDBTransactionsRequested.toString
    val numberDBTransactionsExecuted = TFLInsertPointToPointDuration.numberDBTransactionsExecuted.toString
    val numberDBTransactionsOutstanding = (TFLInsertPointToPointDuration.numberDBTransactionsRequested - TFLInsertPointToPointDuration.numberDBTransactionsExecuted).toString
    val numberDBPullTransactionsExecuted = TFLInsertPointToPointDuration.numberDBPullTransactionsExecuted.toString
    Array(numberInHoldingBuffer, numberDBTransactionsRequested, numberDBTransactionsExecuted, numberDBTransactionsOutstanding, numberDBPullTransactionsExecuted)
  }


}
