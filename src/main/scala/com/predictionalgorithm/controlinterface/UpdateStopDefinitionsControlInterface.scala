package com.predictionalgorithm.controlinterface

import com.predictionalgorithm.datadefinitions.tfl.{LoadStopDefinitions, TFLDefinitions}
import com.predictionalgorithm.database.tfl.TFLInsertStopDefinition

/**
 * User Control Interface for Updating Stop Definitions
 */
object UpdateStopDefinitionsControlInterface extends StartStopControlInterface {
  override def getVariableArray: Array[String] = {
    val percentageComplete = LoadStopDefinitions.percentageComplete.toString
    val numberInserted = TFLInsertStopDefinition.numberDBInsertsRequested.toString
    val numberUpdated = TFLInsertStopDefinition.numberDBUpdatesRequested.toString
    Array(percentageComplete, numberInserted, numberUpdated)
  }

  override def stop(): Unit = throw new IllegalArgumentException("Unable to stop Update Stop Definitions From Web (will leave with incomplete data)")

  override def start(): Unit = {
    TFLDefinitions.updateStopDefinitionsFromWeb()
  }
}
