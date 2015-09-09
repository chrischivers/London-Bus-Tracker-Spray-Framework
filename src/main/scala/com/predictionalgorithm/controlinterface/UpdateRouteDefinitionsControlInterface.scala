package com.predictionalgorithm.controlinterface

import com.predictionalgorithm.datadefinitions.tfl.TFLDefinitions
import com.predictionalgorithm.database.tfl.TFLInsertUpdateRouteDefinition
import com.predictionalgorithm.datadefinitions.tfl.loadresources.LoadRouteDefinitions


/**
 * User Control Interface for Updating Route Definition
 */
object UpdateRouteDefinitionsControlInterface extends StartStopControlInterface {

  override def getVariableArray: Array[String] = {
    val percentageComplete = LoadRouteDefinitions.percentageComplete.toString
    val numberInserted = TFLInsertUpdateRouteDefinition.numberDBInsertsRequested.toString
    val numberUpdated = TFLInsertUpdateRouteDefinition.numberDBUpdatesRequested.toString
    Array(percentageComplete, numberInserted, numberUpdated)
  }

  override def stop(): Unit = throw new IllegalArgumentException("Unable to stop Update Route Definitions From Web (will leave with incomplete data)")

  override def start(): Unit = {
  TFLDefinitions.updateRouteDefinitionsFromWeb()
  }
}
