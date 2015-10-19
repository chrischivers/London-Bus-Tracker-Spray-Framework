package com.predictionalgorithm.controlinterface


import com.predictionalgorithm.datadefinitions.tfl.TFLDefinitions
import com.predictionalgorithm.datadefinitions.tools.FetchPolyLines
import com.typesafe.scalalogging.LazyLogging
/**
 * User Control Interface for the Adding Polylines Function
 */
object AddPolyLinesControlInterface extends StartStopControlInterface with LazyLogging {

  /**
   * Gets the variable array for displaying on the User Interface
   * @return An array of the variables to display on the user interface
   */
  override def getVariableArray: Array[String] = {
    val numberLinesRead = FetchPolyLines.numberLinesProcessed.toString
    val numberPolyLinesUpdatedFromWeb = FetchPolyLines.numberPolyLinesUpdatedFromWeb.toString
    val numberPolyLinesUpdatedFromCache = FetchPolyLines.numberPolyLinesUpdatedFromCache.toString
    Array(numberLinesRead, numberPolyLinesUpdatedFromWeb,numberPolyLinesUpdatedFromCache)
  }

  override def stop(): Unit = throw new IllegalStateException("Unable to stop Add PolyLines (will leave with incomplete data)")

  override def start(): Unit = {
    TFLDefinitions.addPolyLinesFromWeb()
  }
}


