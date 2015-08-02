package com.PredictionAlgorithm.Streaming

import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}

import akka.actor.Status.{Failure, Success}
import akka.actor._
import akka.actor.Actor.Receive
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Prediction.{PredictionRequest, KNNPrediction}

import scala.collection.mutable
import scala.concurrent.duration._

//case class LiveStreamResult(nextPointSeq: Int,nextStopCode: String, nextStopName:String, nextStopLate:Double, nextStopLng: Double, timeTilNextStop:Int)


object LiveStreamingCoordinator {

  implicit val actorSystem = ActorSystem("live_streaming")
  implicit val timeout = 1000

  private val stream = new FIFOStream
  private var liveActors = Map[String, ActorRef]()

  def setObjectPosition(liveSourceLine: TFLSourceLine): Unit = {
    val vehicleReg = liveSourceLine.vehicle_Reg
      if (liveActors.contains(vehicleReg)) {
          liveActors(vehicleReg) ! liveSourceLine
      } else {

        val newActor:ActorRef = actorSystem.actorOf(Props(new VehicleActor(vehicleReg, liveSourceLine.route_ID, liveSourceLine.direction_ID)), vehicleReg)
        liveActors = liveActors + (vehicleReg -> newActor)
        newActor ! liveSourceLine //Start it off
    }
  }

  def getNumberLiveActors = liveActors.size

  def getStream = stream.toStream

  def enqueue(vehicle_ID: String, latitude: String, longitude: String)  = stream.enqueue((vehicle_ID,latitude,longitude))

}

// Implementation adapted from Stack Overflow article:
//http://stackoverflow.com/questions/7553270/is-there-a-fifo-stream-in-scala
class FIFOStream {
  private val queue = new LinkedBlockingQueue[Option[(String, String, String)]]

  def toStream: Stream[(String, String, String)] = queue take match {
    case Some((a:String,b:String, c:String)) => Stream cons ( (a,b,c), toStream )
    case None => Stream empty
  }
  def close() = queue add None
  def enqueue(as:(String, String, String)) = queue add Some(as)
}

class VehicleActor(vehicle_ID: String, routeID: String, directionID: Int) extends Actor{

  import context.dispatcher

  var currentPosition:(String,String) = ("0.0", "0.0")
  var predictedPositionQueue: mutable.Queue[(Long, String, String)] = mutable.Queue()
  val stopDefinitions = TFLDefinitions.StopDefinitions

  override def receive: Receive = {
    case sourceLine:TFLSourceLine => processNewLine(sourceLine)
    case "next" =>
      if (predictedPositionQueue.nonEmpty) {
        val head = predictedPositionQueue.dequeue
        in(Duration(System.currentTimeMillis() - head._1, MILLISECONDS)) {
          val lat = head._2
          val lng = head._3
          currentPosition = (lat, lng)
          LiveStreamingCoordinator.enqueue(vehicle_ID, lat, lng)
          self ! "next"
          //TODO KILL if last

        }
      }

  }

  def processNewLine(sourceLine:TFLSourceLine) = {
   // val lat = definitions(sourceLine.stop_Code).latitude
   // val lng = definitions(sourceLine.stop_Code).longitude
   // predictedPositionQueue = predictedPositionQueue.filter(_._1 < sourceLine.arrival_TimeStamp) //remove anythign in the queue ahead of the stream input (redundant)
    val currentStopCode = sourceLine.stop_Code
    val currentStopReference = TFLDefinitions.StopToPointSequenceMap(routeID,directionID,currentStopCode)
    val currentPointNumber = currentStopReference._1
    val currentFirstLast = currentStopReference._2
    val polyLineToNextStop = currentStopReference._3

    val nextStopReference = TFLDefinitions.PointToStopSequenceMap(routeID,directionID,currentPointNumber + 1)
    val nextStopCode = nextStopReference._1

    val predictionRequest = new PredictionRequest(routeID,directionID,currentStopCode,nextStopCode,Commons.getDayCode(System.currentTimeMillis()),Commons.getTimeOffset(System.currentTimeMillis()))
    val predictedDurationToNextStop = KNNPrediction.makePredictionBetweenConsecutivePoints(predictionRequest)

    var goAhead:Boolean = true
    if (currentFirstLast.contains("LAST")) goAhead = false
    if (predictedDurationToNextStop.isEmpty) goAhead = false

    if (goAhead) {
      val decodedPolyLineToNextStop = Commons.decodePolyLine(polyLineToNextStop)
      val eachPointDuration = (predictedDurationToNextStop.get * 1000) / decodedPolyLineToNextStop.length

      decodedPolyLineToNextStop.foreach(x => {
        addToPredictedPositionQueue(sourceLine.arrival_TimeStamp + eachPointDuration.toLong, x._1, x._2)
      })

    }

    self ! "next"
  }

  def addToPredictedPositionQueue(timestamp:Long, latitude: String, longitude: String) = {
    predictedPositionQueue.enqueue((timestamp,latitude,longitude))
  }

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    LiveStreamingCoordinator.actorSystem.scheduler.scheduleOnce(duration)(body)

}
