package com.predictionalgorithm.controlinterface

import akka.actor.{Props, ActorSystem}
import com.predictionalgorithm.processes.tfl.TFLIterateOverArrivalStreamSupervisor
import com.predictionalgorithm.processes.weather.Weather
import grizzled.slf4j.Logger

/**
 * User Control Interface for Stream Processing Control
 */
object StreamProcessingControlInterface extends StartStopControlInterface {

  val logger = Logger[this.type]

  val mb = 1024*1024
  val runtime = Runtime.getRuntime

  val linesNotBeingReadAlertText = "LINES NOT BEING READ AS EXPECTED. POSSIBLE SERVER CRASH."
  val freeMemoryLowAlertText = "FREE MEMORY RUNNING LOW"
  val periodToCheck = 600000
  val MIN_LINES_TOREAD_IN_PERIOD = 10
  var timeStampLastChecked:Long = 0
  var linesReadOnLastCheck:Long = 0


  override def start(): Unit = {
    TFLIterateOverArrivalStreamSupervisor.start()

  }

  override def stop(): Unit = {
    TFLIterateOverArrivalStreamSupervisor.stop()
  }

  /**
   *  Returns memory usage statistics
   *  This code was adapted from http://alvinalexander.com/scala/how-show-memory-ram-use-scala-application-used-free-total-max
   * @return An Array of variables as Strings
   */
  override def getVariableArray: Array[String] = {
    val numberLinesRead = TFLIterateOverArrivalStreamSupervisor.numberProcessed.toString
    val numberReadSinceRestart = TFLIterateOverArrivalStreamSupervisor.numberProcessedSinceRestart.toString
    val currentRainfall = Weather.getCurrentRainfall.toString
    val usedMemory = ((runtime.totalMemory - runtime.freeMemory) / mb).toString
    val freeMemory =  (runtime.freeMemory / mb).toString
    val totalMemory = (runtime.totalMemory / mb).toString
    val maxMemory =  (runtime.maxMemory / mb).toString
    val array = Array(numberLinesRead, numberReadSinceRestart,currentRainfall, usedMemory,freeMemory,totalMemory,maxMemory)
    checkAndSendForEmailAlerting(array)
    array
  }

  def checkAndSendForEmailAlerting(variableArray: Array[String]): Unit = {
    val linesRead = variableArray(0).toLong
    if (System.currentTimeMillis() - periodToCheck > timeStampLastChecked) {
      if (linesRead - linesReadOnLastCheck < MIN_LINES_TOREAD_IN_PERIOD && linesRead != 0) {
        logger.error("No lines being read")
        EmailAlertInterface.sendAlert(linesNotBeingReadAlertText)
      }
      timeStampLastChecked = System.currentTimeMillis()
      linesReadOnLastCheck = linesRead
    }
    if (variableArray(6).toDouble - variableArray(3).toDouble < 200) {
      EmailAlertInterface.sendAlert(freeMemoryLowAlertText)
    }
  }
}
