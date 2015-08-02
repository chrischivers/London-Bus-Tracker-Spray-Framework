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

  def enqueue(vehicle_ID: String, duration: Double, latitude: String, longitude: String)  = stream.enqueue((vehicle_ID, duration.toString, latitude,longitude))

}

// Implementation adapted from Stack Overflow article:
//http://stackoverflow.com/questions/7553270/is-there-a-fifo-stream-in-scala
class FIFOStream {
  private val queue = new LinkedBlockingQueue[Option[(String, String, String, String)]]

  def toStream: Stream[(String, String, String, String)] = queue take match {
    case Some((a:String,b:String, c:String, d:String)) => Stream cons ( (a,b,c,d), toStream )
    case None => Stream empty
  }
  def close() = queue add None
  def enqueue(as:(String, String, String, String)) = queue add Some(as)
}

class VehicleActor(vehicle_ID: String, routeID: String, directionID: Int) extends Actor{

  import context.dispatcher

  var currentPosition:(String,String) = ("0.0", "0.0")

  //Queue is TimeToTransmit, Duration, Lat, Long
  var predictedPositionQueue: mutable.Queue[(Long, Double, String, String)] = mutable.Queue()
  val stopDefinitions = TFLDefinitions.StopDefinitions

  override def receive: Receive = {
    case sourceLine:TFLSourceLine => processNewLine(sourceLine)
    case "next" =>
      if (predictedPositionQueue.nonEmpty) {
        val head = predictedPositionQueue.dequeue
        in(Duration(head._1 - System.currentTimeMillis(), MILLISECONDS)) {
          val dur = head._2
          val lat = head._3
          val lng = head._4
          currentPosition = (lat, lng)
          LiveStreamingCoordinator.enqueue(vehicle_ID,dur, lat, lng)
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
    val currentRouteReference = TFLDefinitions.RouteDefinitionMap(sourceLine.route_ID,sourceLine.direction_ID)
    val currentStopReference = currentRouteReference.filter(x=> x._2 == currentStopCode).head
    val currentPointNumber = currentStopReference._1
    val currentFirstLast = currentStopReference._3

    if (!currentFirstLast.contains("LAST")) {
      val nextStopReference = currentRouteReference.filter(x => x._1 == currentPointNumber + 1).last
      val nextStopCode = nextStopReference._2
      val polyLineToNextStop = currentStopReference._4

      val predictionRequest = new PredictionRequest(routeID, directionID, currentStopCode, nextStopCode, Commons.getDayCode(System.currentTimeMillis()), Commons.getTimeOffset(System.currentTimeMillis()))
      val predictedDurationToNextStop = KNNPrediction.makePredictionBetweenConsecutivePoints(predictionRequest)

        if (!predictedDurationToNextStop.isEmpty) {
        val decodedPolyLineToNextStop = Commons.decodePolyLine(polyLineToNextStop)
        val eachPointDuration = (predictedDurationToNextStop.get * 1000) / decodedPolyLineToNextStop.length

          addToPredictedPositionQueue(sourceLine.arrival_TimeStamp, 0, decodedPolyLineToNextStop.head._1, decodedPolyLineToNextStop.head._2) //First Point

          var acc = 0.0
          decodedPolyLineToNextStop.drop(1).foreach(x => {
          addToPredictedPositionQueue(sourceLine.arrival_TimeStamp + acc.toInt, eachPointDuration, x._1, x._2)
            acc += eachPointDuration
        })

      }
    }

    self ! "next"
  }

  def addToPredictedPositionQueue(timestampToTransmit:Long, duration: Double, latitude: String, longitude: String) = {
    predictedPositionQueue.enqueue((timestampToTransmit,duration, latitude,longitude))
  }

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    LiveStreamingCoordinator.actorSystem.scheduler.scheduleOnce(duration)(body)

}
