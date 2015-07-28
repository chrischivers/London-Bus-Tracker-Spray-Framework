package com.PredictionAlgorithm.Streaming


case class StreamResult(timeSinceStart:Int, pointSeq: Int, prevStopCode: String,prevStopName: String,nextStopCode: String, nextStopName:String,timeTilNextStop:Int)
case class LiveStreamResult(routeID: String, directionID: Int, nextPointSeq: Int,nextStopCode: String, nextStopName:String, nextStopLat:Double, nextStopLng: Double, arrivalTimeStamp:Long, firstLast:Option[String])


trait StreamInterface extends Iterator[StreamResult]
