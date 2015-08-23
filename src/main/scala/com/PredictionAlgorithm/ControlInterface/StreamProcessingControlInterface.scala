package com.PredictionAlgorithm.ControlInterface

import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.Processes.TFL.TFLIterateOverArrivalStream
import com.PredictionAlgorithm.Processes.Weather.Weather

/**
 * User Control Interface for Stream Processing Control
 */
object StreamProcessingControlInterface extends StartStopControlInterface {

  val streamActor = actorSystem.actorOf(Props[TFLIterateOverArrivalStream], name = "TFLArrivalStream")
  val mb = 1024*1024
  val runtime = Runtime.getRuntime


  override def start(): Unit = {
    streamActor ! "start"

  }

  override def stop(): Unit = {
    streamActor ! "stop"
  }

  /**
   *  Returns memory usage statistics
   *  This code was adapted from http://alvinalexander.com/scala/how-show-memory-ram-use-scala-application-used-free-total-max
   * @return An Array of variables as Strings
   */
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
