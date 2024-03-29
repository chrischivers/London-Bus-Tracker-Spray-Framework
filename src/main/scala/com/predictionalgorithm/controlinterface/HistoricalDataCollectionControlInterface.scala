package com.predictionalgorithm.controlinterface

import com.predictionalgorithm.database.tfl.TFLInsertPointToPointDurationSupervisor
import com.predictionalgorithm.processes.tfl.TFLProcessSourceLines

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
    val numberDBTransactionsRequested = TFLInsertPointToPointDurationSupervisor.numberDBTransactionsRequested.toString
    val numberDBTransactionsExecuted = TFLInsertPointToPointDurationSupervisor.numberDBTransactionsExecuted.toString
    val numberDBTransactionsOutstanding = (TFLInsertPointToPointDurationSupervisor.numberDBTransactionsRequested - TFLInsertPointToPointDurationSupervisor.numberDBTransactionsExecuted).toString
    val numberDBTransactionsDroppedDueToOverflow = TFLInsertPointToPointDurationSupervisor.numberDBTransactionsDroppedDueToOverflow.toString
    val numberDBPullTransactionsRequested = TFLInsertPointToPointDurationSupervisor.numberDBPullTransactionsRequested.toString
    val numberDBPullTransactionsExecuted = TFLInsertPointToPointDurationSupervisor.numberDBPullTransactionsExecuted.toString
    Array(numberInHoldingBuffer, numberNonMatches, numberDBTransactionsRequested, numberDBTransactionsExecuted, numberDBTransactionsOutstanding, numberDBTransactionsDroppedDueToOverflow, numberDBPullTransactionsRequested, numberDBPullTransactionsExecuted)
  }


}
