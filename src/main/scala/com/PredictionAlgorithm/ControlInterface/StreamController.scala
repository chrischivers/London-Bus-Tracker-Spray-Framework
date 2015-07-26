package com.PredictionAlgorithm.ControlInterface

import com.PredictionAlgorithm.Prediction.{KNNPrediction, PredictionInterface}
import com.PredictionAlgorithm.Processes.TFL.{TFLProcessSourceLines, TFLIterateOverArrivalStream}
import com.PredictionAlgorithm.Streaming.{LiveStreamingCoordinator, StreamObject}


class StreamController {

  val sizeLiveStreamMap = LiveStreamingCoordinator.getPositionMapSize

  def enableLiveStreamCollection(enable:Boolean) = {
    TFLProcessSourceLines.setLiveStreamCollection(enable)
  }

  def getPositionSnapshotsMap = {
    LiveStreamingCoordinator.getObjectPositionsMap
  }

  def getPositionSnapshotsForRoute(routeID:String, directionID:Int) = {
    LiveStreamingCoordinator.getObjectPositionsMap.filter(x=> x._2.routeID == routeID && x._2.directionID == directionID)
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
