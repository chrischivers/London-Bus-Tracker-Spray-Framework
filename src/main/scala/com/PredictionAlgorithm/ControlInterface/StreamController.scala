package com.PredictionAlgorithm.ControlInterface

import com.PredictionAlgorithm.Prediction.{KNNPrediction, PredictionInterface}
import com.PredictionAlgorithm.Processes.TFL.{TFLProcessSourceLines, TFLIterateOverArrivalStream}
import com.PredictionAlgorithm.Streaming.{LiveStreamResult, LiveStreamingCoordinator, StreamObject}


class StreamController {

  val sizeLiveStreamMap = LiveStreamingCoordinator.getPositionMapSize

  def enableLiveStreamCollection(enable:Boolean) = {
    TFLProcessSourceLines.setLiveStreamCollection(enable)
  }

  def getPositionSnapshotsMap = {
    LiveStreamingCoordinator.getObjectPositionsMap
  }

  def getPositionSnapshotsForRoute(routeID:String) = {
    LiveStreamingCoordinator.getObjectPositionsMap.filter(x=> x._2.routeID == routeID)
  }

  def getStream: Iterator[(String, LiveStreamResult)] = {
    LiveStreamingCoordinator.getStream.iterator
  }

  /*var stream:StreamObject = _

  def setUpNewStream(routeID: String, direction:Int) = {
    stream = new StreamObject(routeID,direction,"1",System.currentTimeMillis())
  }

  def getCurrentPosition = {
    if (stream == null) {
        setUpNewStream("3",2)
    }
    stream.next()
  }*/


}
