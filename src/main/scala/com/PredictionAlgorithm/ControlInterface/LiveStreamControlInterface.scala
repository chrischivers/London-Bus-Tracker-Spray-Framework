package com.PredictionAlgorithm.ControlInterface

import com.PredictionAlgorithm.Prediction.{KNNPrediction, PredictionInterface}
import com.PredictionAlgorithm.Processes.TFL.{TFLProcessSourceLines, TFLIterateOverArrivalStream}
import com.PredictionAlgorithm.Streaming.{PackagedStreamObject, LiveStreamResult, LiveStreamingCoordinator, LiveStream}


object LiveStreamControlInterface extends StartStopControlInterface {

  def getStream: Iterator[PackagedStreamObject] = {
    LiveStreamingCoordinator.getStream.iterator
  }


  override def getVariableArray: Array[String] = {
    val numberLiveActors  = LiveStreamingCoordinator.getNumberLiveActors.toString
    Array(numberLiveActors)
  }

  override def stop: Unit = TFLProcessSourceLines.setLiveStreamCollection(false)

  override def start: Unit = TFLProcessSourceLines.setLiveStreamCollection(true)
}
