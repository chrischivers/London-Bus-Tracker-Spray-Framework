package com.PredictionAlgorithm.Streaming

import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine

case class livePositionData(routeID: String, directionID: Int, pointSequence: Int, nextStopID: String, arrivalTime: Long, firstLast:Option[String])

trait LiveStreamingCoordinatorInterface {

  //Reg_No -> routeID, direction ID, pointSequence, nextStopCode, timeUntilNextStop
  var livePositionMap:Map[String,livePositionData]

  // Map of RegNumber (Unique vehicle identifier) to Stream Result
  def getObjectPositionsMap: Map[String, LiveStreamResult]

  def setObjectPosition(liveSourceLine: TFLSourceLine)

  def getPositionMapSize: Int = livePositionMap.size

}
