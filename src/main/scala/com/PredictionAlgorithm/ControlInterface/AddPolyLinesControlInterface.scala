package com.PredictionAlgorithm.ControlInterface

import akka.actor.Props
import com.PredictionAlgorithm.ControlInterface.UpdateRouteDefinitionsControlInterface._
import com.PredictionAlgorithm.DataDefinitions.TFL.{LoadRouteDefinitions, TFLDefinitions}
import com.PredictionAlgorithm.DataDefinitions.Tools.FetchPolyLines
import com.PredictionAlgorithm.Database.TFL.TFLInsertUpdateRouteDefinition$


object AddPolyLinesControlInterface extends StartStopControlInterface {

  override def getVariableArray: Array[String] = {
    val numberLinesRead = FetchPolyLines.numberLinesProcessed.toString
    val numberPolyLinesUpdatedFromWeb = FetchPolyLines.numberPolyLinesUpdatedFromWeb.toString
    val numberPolyLinesUpdatedFromCache = FetchPolyLines.numberPolyLinesUpdatedFromCache.toString
    Array(numberLinesRead, numberPolyLinesUpdatedFromWeb,numberPolyLinesUpdatedFromCache)
  }

  override def stop: Unit = throw new IllegalArgumentException("Unable to stop Add PolyLines (will leave with incomplete data)")

  override def start: Unit = {
    TFLDefinitions.addPolyLinesFromWeb
  }
}


