package com.PredictionAlgorithm.Streaming

import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Prediction.{RouteListVariables, RoutePredictionMapping}



//TODO add starting point
class LiveStream(val routeID: String, val direction:Int, val startPoint:String, val startTime:Long) extends StreamInterface {
  //TODO do something with startPoint
  val startTimeOffset = Commons.getTimeOffset(startTime)
  val startDay = Commons.getDayCode(startTime)
  var atEnd: Boolean = false

  val routeList = RoutePredictionMapping.getRoutePredictionMap(routeID,direction,startDay,startTimeOffset).getOrElse(
    throw new InstantiationError("Cannot get route map for routeID: " + routeID + ", direction: " + direction + ", start point: " + startDay + ", startTimeOffset: " + startTimeOffset))
  var accumulator = 0.0
  val cumulativeRouteList = routeList.map { case (rlv:RouteListVariables) => {
    accumulator += rlv.duration
    new RouteListVariables(rlv.pointSeq, rlv.fromStop, rlv.toStop, accumulator)
  }
  }


  override def hasNext: Boolean = !atEnd

  //returns time since start, pointSeq, Stop Code, and Time until
  override def next(): StreamResult = {
      val timeSinceStart = (Commons.getTimeOffset(System.currentTimeMillis()) - startTimeOffset)
    try {
      val nextListEntry = cumulativeRouteList.filter(x => x.duration >= timeSinceStart)(0) //Filter the routeList to remove entries already passed before the current time. Get the first entry
      val timeTilNextStop = nextListEntry.duration - timeSinceStart
      val nextStopName = TFLDefinitions.StopDefinitions(nextListEntry.toStop).stopPointName
      val prevStopName = TFLDefinitions.StopDefinitions(nextListEntry.fromStop).stopPointName
      new StreamResult(timeSinceStart, nextListEntry.pointSeq,nextListEntry.fromStop, prevStopName, nextListEntry.toStop,nextStopName, timeTilNextStop.toInt)
    } catch {
      case e: IndexOutOfBoundsException => println("Error on streaming. Index out of bounds. Map printing: \n" + routeList);
        new StreamResult(timeSinceStart, 0,"Error", "Error", "Error","Error", 0)
    }
  }
}
