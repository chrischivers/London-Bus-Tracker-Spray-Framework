package com.PredictionAlgorithm.ControlInterface

import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.Database.TFL.TFLInsertPointToPointDuration
import com.PredictionAlgorithm.Processes.TFL.{TFLProcessSourceLines, TFLIterateOverArrivalStream}
import com.PredictionAlgorithm.Processes.Weather.Weather


object StreamProcessingControlInterface extends StartStopControlInterface {

  // Memory usage code borrowed from http://alvinalexander.com/scala/how-show-memory-ram-use-scala-application-used-free-total-max

  val streamActor = actorSystem.actorOf(Props[TFLIterateOverArrivalStream], name = "TFLArrivalStream")
  val mb = 1024*1024
  val runtime = Runtime.getRuntime


  override def start: Unit = {
    streamActor ! "start"

  }

  override def stop: Unit = {
    streamActor ! "stop"
  }

  override def getVariableArray: Array[String] = {
    val numberLinesRead = TFLIterateOverArrivalStream.numberProcessed.toString
    val currentRainfall = Weather.getCurrentRainfall.toString
    val usedMemory = ((runtime.totalMemory - runtime.freeMemory) / mb).toString
    val freeMemory =  (runtime.freeMemory / mb).toString
    val totalMemory = (runtime.totalMemory / mb).toString
    val maxMemory =  (runtime.maxMemory / mb).toString
    Array(numberLinesRead, currentRainfall, usedMemory,freeMemory,totalMemory,maxMemory)
  }


}
