package com.PredictionAlgorithm.Streaming

import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine

//case class LiveStreamResult(nextPointSeq: Int,nextStopCode: String, nextStopName:String, nextStopLate:Double, nextStopLng: Double, timeTilNextStop:Int)


object LiveStreamingCoordinator extends LiveStreamingCoordinatorInterface{

  val DELETE_TIME_THRESHOLD_MS = 120000

  override var livePositionMap: Map[String, livePositionData] = Map()

  // Map of RegNumber (Unique vehicle identifier) to Stream Result
  override def getObjectPositionsMap: Map[String, LiveStreamResult] = {
    livePositionMap.map(x=> x._1 -> {
      val stopDefinition = TFLDefinitions.StopDefinitions(x._2.nextStopID)
      new LiveStreamResult(x._2.routeID, x._2.directionID ,x._2.pointSequence,x._2.nextStopID, stopDefinition.stopPointName, stopDefinition.latitude, stopDefinition.longitude, x._2.arrivalTime)
    })
  }

  override def setObjectPosition(liveSourceLine: TFLSourceLine): Unit = {
    val pointSequenceFirstLast = TFLDefinitions.StopToPointSequenceMap.get(liveSourceLine.route_ID,liveSourceLine.direction_ID,liveSourceLine.stop_Code)
    val pointSequence = pointSequenceFirstLast.get._1
    val firstLast = pointSequenceFirstLast.get._2
    livePositionMap += (liveSourceLine.vehicle_Reg -> new livePositionData(liveSourceLine.route_ID,liveSourceLine.direction_ID,pointSequence,liveSourceLine.stop_Code,liveSourceLine.arrival_TimeStamp,firstLast))
    livePositionMap = livePositionMap.filter(x => System.currentTimeMillis() - x._2.arrivalTime < DELETE_TIME_THRESHOLD_MS) //TODO this will change once prediction Algorithm introduced
  }

}
