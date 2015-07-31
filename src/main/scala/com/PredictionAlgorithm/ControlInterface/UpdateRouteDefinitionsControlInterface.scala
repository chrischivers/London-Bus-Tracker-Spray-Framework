package com.PredictionAlgorithm.ControlInterface

import akka.actor.Props
import com.PredictionAlgorithm.ControlInterface.StreamProcessingControlInterface._
import com.PredictionAlgorithm.DataDefinitions.TFL.{LoadRouteDefinitions, TFLDefinitions}
import com.PredictionAlgorithm.Database.TFL.{TFLInsertUpdateRouteDefinitionDocument, TFLInsertPointToPointDuration}
import com.PredictionAlgorithm.Processes.TFL.{TFLIterateOverArrivalStream, TFLProcessSourceLines}

/**
 * Created by chrischivers on 31/07/15.
 */
object UpdateRouteDefinitionsControlInterface extends StartStopControlInterface {

  val streamActor = actorSystem.actorOf(Props[TFLIterateOverArrivalStream], name = "TFLArrivalStream")

  override def getVariableArray: Array[String] = {
    val percentageComplete = LoadRouteDefinitions.percentageComplete.toString
    val numberInserted = TFLInsertUpdateRouteDefinitionDocument.numberDBInsertsRequested.toString
    val numberUpdated = TFLInsertUpdateRouteDefinitionDocument.numberDBUpdatesRequested.toString
    Array(percentageComplete, numberInserted, numberUpdated)
  }

  override def stop: Unit = throw new IllegalArgumentException("Unable to stop Update Route Definitions From Web (will leave with incomplete data)")

  override def start: Unit = {
  TFLDefinitions.updateRouteDefinitionsFromWeb
  }
}
