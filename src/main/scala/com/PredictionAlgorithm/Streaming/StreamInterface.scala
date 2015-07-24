package com.PredictionAlgorithm.Streaming


case class StreamResult(val timeSinceStart:Int, val pointSeq: Int, val prevStopCode: String,val prevStopName: String,val nextStopCode: String, val nextStopName:String,val timeTilNextStop:Int)


trait StreamInterface extends Iterator[StreamResult]
