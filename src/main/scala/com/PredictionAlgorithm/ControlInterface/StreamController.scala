package com.PredictionAlgorithm.ControlInterface

import com.PredictionAlgorithm.Prediction.{KNNPrediction, PredictionInterface}
import com.PredictionAlgorithm.Streaming.StreamObject


class StreamController {

  var stream:StreamObject = _

  def setUpNewStream(routeID: String, direction:Int) = {
    stream = new StreamObject(routeID,direction,"1",System.currentTimeMillis())
  }

  def getCurrentPosition = {
    if (stream == null) {
        setUpNewStream("3",2)
    }
    stream.next()
  }


}
