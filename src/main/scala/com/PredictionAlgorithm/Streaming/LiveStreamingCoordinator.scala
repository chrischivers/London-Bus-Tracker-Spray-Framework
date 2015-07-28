package com.PredictionAlgorithm.Streaming

import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}

import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine

//case class LiveStreamResult(nextPointSeq: Int,nextStopCode: String, nextStopName:String, nextStopLate:Double, nextStopLng: Double, timeTilNextStop:Int)


object LiveStreamingCoordinator extends LiveStreamingCoordinatorInterface{

  val DELETE_TIME_THRESHOLD_MS = 120000
  val x = new FIFOStream
  val definitions = TFLDefinitions.StopDefinitions

  override var livePositionMap: Map[String, livePositionData] = Map()

  // Map of RegNumber (Unique vehicle identifier) to Stream Result
  override def getObjectPositionsMap: Map[String, LiveStreamResult] = {
    livePositionMap.map(x=> x._1 -> {
      val stopDefinition = TFLDefinitions.StopDefinitions(x._2.nextStopID)
      new LiveStreamResult(x._2.routeID, x._2.directionID ,x._2.pointSequence,x._2.nextStopID, stopDefinition.stopPointName, stopDefinition.latitude, stopDefinition.longitude, x._2.arrivalTime, x._2.firstLast)
    })
  }

  override def setObjectPosition(liveSourceLine: TFLSourceLine): Unit = {
    val pointSequenceFirstLast = TFLDefinitions.StopToPointSequenceMap.get(liveSourceLine.route_ID,liveSourceLine.direction_ID,liveSourceLine.stop_Code)
    val pointSequence = pointSequenceFirstLast.get._1
    val firstLast = pointSequenceFirstLast.get._2
    livePositionMap += (liveSourceLine.vehicle_Reg -> new livePositionData(liveSourceLine.route_ID,liveSourceLine.direction_ID,pointSequence,liveSourceLine.stop_Code,liveSourceLine.arrival_TimeStamp,firstLast))
    livePositionMap = livePositionMap.filter(x => System.currentTimeMillis() - x._2.arrivalTime < DELETE_TIME_THRESHOLD_MS) //TODO this will change once prediction Algorithm introduced

    x.enqueue((liveSourceLine.vehicle_Reg -> new LiveStreamResult(liveSourceLine.route_ID,liveSourceLine.direction_ID,pointSequence,liveSourceLine.stop_Code,definitions(liveSourceLine.stop_Code).stopPointName, definitions(liveSourceLine.stop_Code).latitude, definitions(liveSourceLine.stop_Code).longitude, liveSourceLine.arrival_TimeStamp,firstLast)))
  }

  def getStream = x.toStream

}

// Implementation adapted from Stack Overflow article:
//http://stackoverflow.com/questions/7553270/is-there-a-fifo-stream-in-scala
class FIFOStream {
  private val queue = new LinkedBlockingQueue[Option[(String, LiveStreamResult)]]

  def toStream: Stream[(String, LiveStreamResult)] = queue take match {
    case Some((a:String,b:LiveStreamResult)) => Stream cons ( (a,b), toStream )
    case None => Stream empty
  }
  def close() = queue add None
  def enqueue( as: (String, LiveStreamResult) ) = queue add (Some(as) )
}
