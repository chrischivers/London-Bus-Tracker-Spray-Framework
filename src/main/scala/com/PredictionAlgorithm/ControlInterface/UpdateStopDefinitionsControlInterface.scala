package com.PredictionAlgorithm.ControlInterface

import com.PredictionAlgorithm.DataDefinitions.TFL.{LoadStopDefinitions, LoadRouteDefinitions, TFLDefinitions}
import com.PredictionAlgorithm.Database.TFL.{TFLInsertStopDefinition, TFLInsertUpdateRouteDefinition}

/**
 * Created by chrischivers on 31/07/15.
 */
object UpdateStopDefinitionsControlInterface extends StartStopControlInterface {
  override def getVariableArray: Array[String] = {
    val percentageComplete = LoadStopDefinitions.percentageComplete.toString
    val numberInserted = TFLInsertStopDefinition.numberDBInsertsRequested.toString
    val numberUpdated = TFLInsertStopDefinition.numberDBUpdatesRequested.toString
    Array(percentageComplete, numberInserted, numberUpdated)
  }

  override def stop: Unit = throw new IllegalArgumentException("Unable to stop Update Stop Definitions From Web (will leave with incomplete data)")

  override def start: Unit = {
    TFLDefinitions.updateStopDefinitionsFromWeb
  }
}
