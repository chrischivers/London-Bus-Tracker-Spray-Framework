package com.PredictionAlgorithm.Streaming

import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}

import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine

//case class LiveStreamResult(nextPointSeq: Int,nextStopCode: String, nextStopName:String, nextStopLate:Double, nextStopLng: Double, timeTilNextStop:Int)


object LiveStreamingCoordinator extends LiveStreamingCoordinatorInterface{

  val DELETE_TIME_THRESHOLD_MS = 120000
  val x = new FIFOStream[(String,livePositionData)](FIFOStream[Option[(String,livePositionData)]]())

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

    x.enqueue((liveSourceLine.vehicle_Reg -> new livePositionData(liveSourceLine.route_ID,liveSourceLine.direction_ID,pointSequence,liveSourceLine.stop_Code,liveSourceLine.arrival_TimeStamp,firstLast)))
  }

  def getStream = x.toStream

}

class FIFOStream[A]( private val queue: BlockingQueue[Option[A]] ) {
  def toStream: Stream[A] = queue take match {
    case Some(a) => Stream cons ( a, toStream )
    case None => Stream empty
  }
  def close() = queue add None
  def enqueue( as: A ) = queue add (Some(as) )
}

object FIFOStream {
  def apply[A]() = new LinkedBlockingQueue[A]
}
