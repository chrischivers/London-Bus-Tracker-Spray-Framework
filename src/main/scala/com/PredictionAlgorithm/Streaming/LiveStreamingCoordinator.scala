package com.PredictionAlgorithm.Streaming

import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}

import akka.actor.Status.{Failure, Success}
import akka.actor._
import akka.actor.Actor.Receive
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Prediction.{PredictionRequest, KNNPrediction}

import scala.collection.{SortedMap, mutable}
import scala.concurrent.duration._

//case class LiveStreamResult(nextPointSeq: Int,nextStopCode: String, nextStopName:String, nextStopLate:Double, nextStopLng: Double, timeTilNextStop:Int)


object LiveStreamingCoordinator {

  implicit val actorSystem = ActorSystem("live_streaming")
  implicit val timeout = 1000

  private val stream = new FIFOStream
  private var liveActors = Map[String, ActorRef]()
  private var inputsReceivedCache: List[(String, String, Int, String, Long)] = List()
  private val CACHE_HOLD_FOR_TIME = 600000

  def setObjectPosition(liveSourceLine: TFLSourceLine): Unit = {
    //if (liveSourceLine.route_ID == "3") {
      // This checks it is not aready in the cache
      if (!inputsReceivedCache.exists(x => x._1 == liveSourceLine.vehicle_Reg && x._2 == liveSourceLine.route_ID && x._3 == liveSourceLine.direction_ID && x._4 == liveSourceLine.stop_Code)) {
        inputsReceivedCache = inputsReceivedCache :+(liveSourceLine.vehicle_Reg, liveSourceLine.route_ID, liveSourceLine.direction_ID, liveSourceLine.stop_Code, System.currentTimeMillis())
        inputsReceivedCache = inputsReceivedCache.filter(x => x._5 > (System.currentTimeMillis() - CACHE_HOLD_FOR_TIME))

        val vehicleReg = liveSourceLine.vehicle_Reg
        if (liveActors.contains(vehicleReg)) {
          liveActors(vehicleReg) ! liveSourceLine
        } else {

          val newActor: ActorRef = actorSystem.actorOf(Props(new VehicleActor(vehicleReg, liveSourceLine.route_ID, liveSourceLine.direction_ID)), vehicleReg)
          liveActors = liveActors + (vehicleReg -> newActor)
          newActor ! liveSourceLine //Start it off
        }
      }
    //}
  }

  def getNumberLiveActors = liveActors.size

  def killLiveActor (reg:String): Unit = {
    liveActors(reg) ! PoisonPill
    liveActors = liveActors - reg //Remove from map
  }

  def getStream = stream.toStream

  def enqueue(vehicle_ID: String, duration: Double, latLngArray: Array[(String, String)]) = stream.enqueue((vehicle_ID, duration.toString, latLngArray))

}

// Implementation adapted from Stack Overflow article:
//http://stackoverflow.com/questions/7553270/is-there-a-fifo-stream-in-scala
class FIFOStream {
  //StartAtTimestamp, duration, [lat lng],
  private val queue = new LinkedBlockingQueue[Option[(String, String, Array[(String, String)])]]

  def toStream: Stream[(String, String, Array[(String, String)])] = queue take match {
    case Some((a: String, b: String, c: Array[(String, String)])) => Stream cons((a, b, c), toStream)
    case None => Stream empty
  }

  def close() = queue add None

  def enqueue(as: (String, String, Array[(String, String)])) = queue add Some(as)
}


/*
class VehicleActor(vehicle_ID: String, routeID: String, directionID: Int) extends Actor {

  import context.dispatcher

  //Queue is TimeToTransmit, Duration, Lat, Long
  var predictedPositionQueue: mutable.Queue[(Long, Double, Array[(String, String)])] = mutable.Queue()
  val stopDefinitions = TFLDefinitions.StopDefinitions

  override def receive: Receive = {
    case sourceLine: TFLSourceLine => processNewLine(sourceLine)
    case "next" =>
      if (predictedPositionQueue.nonEmpty) {
        val head = predictedPositionQueue.dequeue
        in(Duration(head._1 - System.currentTimeMillis(), MILLISECONDS)) {
          val dur = head._2
          val latLng = head._3
          LiveStreamingCoordinator.enqueue(vehicle_ID, dur, latLng)
          self ! "next"
          //TODO KILL if last

        }
      }

  }

  def processNewLine(sourceLine: TFLSourceLine) = {
    // val lat = definitions(sourceLine.stop_Code).latitude
    // val lng = definitions(sourceLine.stop_Code).longitude
    // predictedPositionQueue = predictedPositionQueue.filter(_._1 < sourceLine.arrival_TimeStamp) //remove anythign in the queue ahead of the stream input (redundant)
    val currentStopCode = sourceLine.stop_Code
    val currentRouteReference = TFLDefinitions.RouteDefinitionMap(sourceLine.route_ID, sourceLine.direction_ID)
    val currentStopReference = currentRouteReference.filter(x => x._2 == currentStopCode).head
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

        addToPredictedPositionQueue(sourceLine.arrival_TimeStamp, predictedDurationToNextStop.get * 1000, decodedPolyLineToNextStop)


      }
    }

    self ! "next"
  }

  def addToPredictedPositionQueue(timestampToTransmit: Long, duration: Double, latLngArray: Array[(String, String)]) = {
    predictedPositionQueue.enqueue((timestampToTransmit, duration, latLngArray))
  }

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    LiveStreamingCoordinator.actorSystem.scheduler.scheduleOnce(duration)(body)

}*/


class VehicleActor(vehicle_ID: String, routeID: String, directionID: Int) extends Actor {

  import context.dispatcher

  // Sequence, FromStop ToStop -> TimeExpected, Duration, PolyLine
  var routeTree: SortedMap[(Int, String, String), Option[(Long, Double, String)]] = SortedMap()
  val stopList = TFLDefinitions.RouteDefinitionMap(routeID, directionID).sortBy(_._1)


  // Builds Tree
  for (i <- 0 until stopList.length - 1) {
    routeTree += (i + 1, stopList(i)._2, stopList(i + 1)._2) -> None
  }

  assert(TFLDefinitions.RouteDefinitionMap(routeID, directionID).head._2 == routeTree.head._1._2)
  assert(TFLDefinitions.RouteDefinitionMap(routeID, directionID).last._2 == routeTree.last._1._3)


  override def receive: Receive = {
    case sourceLine: TFLSourceLine => processSourceLine(sourceLine)
    case "next" => pushNextToQueue

  }

  var alreadyreceived = false
  def processSourceLine(sourceLine: TFLSourceLine) = {
    if (!alreadyreceived) {
      //  println("Before processing: " + routeTree)
      val fromStopCode = sourceLine.stop_Code
      val arrivalTimeStamp = sourceLine.arrival_TimeStamp
      val nextCalculatedArrivalStamp = calculateAndUpdateTree(fromStopCode, arrivalTimeStamp)
      if (nextCalculatedArrivalStamp.isDefined) calculateFuturePointPredictions(nextCalculatedArrivalStamp.get._1, nextCalculatedArrivalStamp.get._2)

      self ! "next"
      alreadyreceived = true
    }

}

  def pushNextToQueue = {
    //TODO better way of dealing with empties
    val routeTreeWithValues = routeTree.filter(x => x._2.isDefined)
    val routeTreeFilteredOutPast = routeTreeWithValues.filter(x => (x._2.get._1 - System.currentTimeMillis()) > 0)
    if (routeTreeFilteredOutPast.nonEmpty) {
      val nextToEnter = routeTreeFilteredOutPast.head
      val firstLast = stopList.filter(x => x._2 == nextToEnter._1._3).head._3

      in(Duration(nextToEnter._2.get._1 - System.currentTimeMillis(), MILLISECONDS)) {
        val dur = nextToEnter._2.get._2
        val decodedPolyLineToNextStop = Commons.decodePolyLine(nextToEnter._2.get._3)
        LiveStreamingCoordinator.enqueue(vehicle_ID, dur, decodedPolyLineToNextStop)
        if (firstLast.contains("LAST")) {
          //TODO enqueue kill actor instruction
          LiveStreamingCoordinator.killLiveActor(vehicle_ID)
        }
        self ! "next"
      }
    } else {
       //TODO think
    }
  }


  def calculateFuturePointPredictions(setPointSequence: Int, nextCalculatedArrivalStamp:Long):Boolean = {

    var nextTimeStampHolder = nextCalculatedArrivalStamp

    for( i <- setPointSequence + 1 to routeTree.size){
      val thisRecord = routeTree.filter(x=> x._1._1 == i).head
      val fromStopCode= thisRecord._1._2
      val nextTimeStamp = calculateAndUpdateTree(fromStopCode, nextTimeStampHolder)
      if (nextTimeStamp.isDefined) {
        nextTimeStampHolder = nextTimeStamp.get._2

        if(nextTimeStamp.get._1 != i + 1) {
          println(nextTimeStamp.get._1 +", " +  (i + 1))
          println("routeID: " + routeID + ". Direction: " + directionID + ". Vehicle ID: " + vehicle_ID)
          println("Stop List: " + stopList)
          println("route tree :" + routeTree)
        }
      } else return false

    }
    true
  }

  def calculateAndUpdateTree(fromStopCode:String,arrivalTimeStamp:Long):Option[(Int, Long)] = {
    try {
      stopList.filter(x => x._2 == fromStopCode).head._3
    } catch {
      case e:Exception =>     println("routeID: " + routeID + ". Direction: " + directionID + ". From stop code: " + fromStopCode + ". stopList: " + stopList)
    }
    val firstLast = stopList.filter(x => x._2 == fromStopCode).head._3
      if (!firstLast.contains("LAST")) {
        val thisRecord = routeTree.filter(x => x._1._2 == fromStopCode).head
        val pointSequence = thisRecord._1._1
        val dayCode = Commons.getDayCode(arrivalTimeStamp)
        val timeOffset = Commons.getTimeOffset(arrivalTimeStamp)
        val toStopCode = thisRecord._1._3
        val polyLine = stopList.filter(x => x._2 == fromStopCode).head._4
        val predictedDuration = KNNPrediction.makePredictionBetweenConsecutivePoints(new PredictionRequest(routeID, directionID, fromStopCode, toStopCode, dayCode, timeOffset))

        if (predictedDuration.isDefined) {
          val keyToUse = (pointSequence, fromStopCode, toStopCode)
          val mapValueToInsert = (arrivalTimeStamp, predictedDuration.get * 1000, polyLine)
          routeTree += keyToUse -> Some(mapValueToInsert)
          return Some(pointSequence + 1, arrivalTimeStamp + (predictedDuration.get.toInt * 1000))
        } else {
          //TODO
          None
        }
      } else None
    }


  def in[U](duration: FiniteDuration)(body: => U): Unit =
    LiveStreamingCoordinator.actorSystem.scheduler.scheduleOnce(duration)(body)

}