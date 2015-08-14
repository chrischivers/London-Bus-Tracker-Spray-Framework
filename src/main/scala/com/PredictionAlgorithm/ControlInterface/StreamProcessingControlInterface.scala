package com.PredictionAlgorithm.ControlInterface

import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.Database.TFL.TFLInsertPointToPointDuration
import com.PredictionAlgorithm.Processes.TFL.{TFLProcessSourceLines, TFLIterateOverArrivalStream}
import com.PredictionAlgorithm.Processes.Weather.Weather


object StreamProcessingControlInterface extends StartStopControlInterface {

  val streamActor = actorSystem.actorOf(Props[TFLIterateOverArrivalStream], name = "TFLArrivalStream")


  override def start: Unit = {
    streamActor ! "start"

  }

  override def stop: Unit = {
    streamActor ! "stop"
  }

  override def getVariableArray: Array[String] = {
    val numberLinesRead = TFLIterateOverArrivalStream.numberProcessed.toString
    val currentRainfall = Weather.getCurrentRainfall.toString
    Array(numberLinesRead, currentRainfall)
  }


}
