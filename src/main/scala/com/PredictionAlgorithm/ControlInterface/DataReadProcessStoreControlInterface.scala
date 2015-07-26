package com.PredictionAlgorithm.ControlInterface

import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.Database.TFL.TFLInsertPointToPointDuration
import com.PredictionAlgorithm.Processes.TFL.{TFLProcessSourceLines, TFLIterateOverArrivalStream}


object DataReadProcessStoreControlInterface extends StartStopControlInterface {

  val streamActor = actorSystem.actorOf(Props[TFLIterateOverArrivalStream], name = "TFLArrivalStream")


  override def start: Unit = {
    streamActor ! "start"

  }

  override def stop: Unit = {
    streamActor ! "stop"
  }

  override def getVariableArray: Array[String] = {
    val numberLinesRead = TFLIterateOverArrivalStream.numberProcessed.toString
    val numberInHoldingBuffer = TFLProcessSourceLines.getBufferSize.toString
    val numberDBTransactionsRequested = TFLInsertPointToPointDuration.numberDBTransactionsRequested.toString
    val numberDBTransactionsExecuted = TFLInsertPointToPointDuration.numberDBTransactionsExecuted.toString
    val numberDBTransactionsOutstanding = (TFLInsertPointToPointDuration.numberDBTransactionsRequested - TFLInsertPointToPointDuration.numberDBTransactionsExecuted).toString
    val numberDBPullTransactionsExecuted = TFLInsertPointToPointDuration.numberDBPullTransactionsExecuted.toString
    Array(numberLinesRead, numberInHoldingBuffer, numberDBTransactionsRequested, numberDBTransactionsExecuted, numberDBTransactionsOutstanding, numberDBPullTransactionsExecuted)
  }


}
