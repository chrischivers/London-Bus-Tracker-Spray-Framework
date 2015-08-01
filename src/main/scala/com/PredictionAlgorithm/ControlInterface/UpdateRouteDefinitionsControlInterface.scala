package com.PredictionAlgorithm.ControlInterface

import akka.actor.Props
import com.PredictionAlgorithm.ControlInterface.StreamProcessingControlInterface._
import com.PredictionAlgorithm.DataDefinitions.TFL.{LoadRouteDefinitions, TFLDefinitions}
import com.PredictionAlgorithm.Database.TFL.{TFLInsertUpdateRouteDefinition, TFLInsertUpdateRouteDefinition$, TFLInsertPointToPointDuration}
import com.PredictionAlgorithm.Processes.TFL.{TFLIterateOverArrivalStream, TFLProcessSourceLines}

/**
 * Created by chrischivers on 31/07/15.
 */
object UpdateRouteDefinitionsControlInterface extends StartStopControlInterface {

  override def getVariableArray: Array[String] = {
    val percentageComplete = LoadRouteDefinitions.percentageComplete.toString
    val numberInserted = TFLInsertUpdateRouteDefinition.numberDBInsertsRequested.toString
    val numberUpdated = TFLInsertUpdateRouteDefinition.numberDBUpdatesRequested.toString
    Array(percentageComplete, numberInserted, numberUpdated)
  }

  override def stop: Unit = throw new IllegalArgumentException("Unable to stop Update Route Definitions From Web (will leave with incomplete data)")

  override def start: Unit = {
  TFLDefinitions.updateRouteDefinitionsFromWeb
  }
}
