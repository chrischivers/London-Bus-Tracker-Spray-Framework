package com.PredictionAlgorithm.ControlInterface

import akka.actor.Props
import com.PredictionAlgorithm.ControlInterface.UpdateRouteDefinitionsControlInterface._
import com.PredictionAlgorithm.DataDefinitions.TFL.{LoadRouteDefinitions, TFLDefinitions}
import com.PredictionAlgorithm.DataDefinitions.Tools.AddPolyLines
import com.PredictionAlgorithm.Database.TFL.TFLInsertUpdateRouteDefinitionDocument


object AddPolyLinesControlInterface extends StartStopControlInterface {

  override def getVariableArray: Array[String] = {
    val numberLinesRead = AddPolyLines.numberLinesProcessed.toString
    val numberPolyLinesProcessed = AddPolyLines.numberPolyLinesUpdated.toString
    Array(numberLinesRead, numberPolyLinesProcessed)
  }

  override def stop: Unit = throw new IllegalArgumentException("Unable to stop Add PolyLines From Web (will leave with incomplete data)")

  override def start: Unit = {
    TFLDefinitions.addPolyLinesFromWeb
  }
}


