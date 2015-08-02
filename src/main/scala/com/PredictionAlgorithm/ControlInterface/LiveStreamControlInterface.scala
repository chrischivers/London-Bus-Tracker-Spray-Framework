package com.PredictionAlgorithm.ControlInterface

import com.PredictionAlgorithm.Prediction.{KNNPrediction, PredictionInterface}
import com.PredictionAlgorithm.Processes.TFL.{TFLProcessSourceLines, TFLIterateOverArrivalStream}
import com.PredictionAlgorithm.Streaming.{LiveStreamResult, LiveStreamingCoordinator, StreamObject}


object LiveStreamControlInterface extends StartStopControlInterface {

  def getStream: Iterator[(String, String, String, String)] = {
    LiveStreamingCoordinator.getStream.iterator
  }


  override def getVariableArray: Array[String] = {
    val numberLiveActors  = LiveStreamingCoordinator.getNumberLiveActors.toString
    Array(numberLiveActors)
  }

  override def stop: Unit = TFLProcessSourceLines.setLiveStreamCollection(false)

  override def start: Unit = TFLProcessSourceLines.setLiveStreamCollection(true)
}
