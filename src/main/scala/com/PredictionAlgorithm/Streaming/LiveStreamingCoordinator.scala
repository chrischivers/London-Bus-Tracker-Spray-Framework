package com.PredictionAlgorithm.Streaming

import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}

import akka.actor.Status.{Failure, Success}
import akka.actor._
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Prediction.{PredictionRequest, KNNPrediction}

import scala.collection.{SortedMap, mutable}
import scala.concurrent.duration._


object LiveStreamingCoordinator {

  implicit val actorSystem = ActorSystem("live_streaming")
  implicit val timeout = 1000

  private val stream = new FIFOStream
  private var liveActors = Map[String, (ActorRef, Long)]()
  private var inputsReceivedCache: List[(String, String, Int, String, Long)] = List()
  private val CACHE_HOLD_FOR_TIME = 600000
  private val IDLE_TIME_UNTIL_ACTOR_KILLED = 600000
  @volatile private var cleaningInProgress: Boolean = false

  def setObjectPosition(liveSourceLine: TFLSourceLine): Unit = {
    // if (liveSourceLine.route_ID == "3") {
    // This checks it is not aready in the cache
    if (!inputsReceivedCache.exists(x => x._1 == liveSourceLine.vehicle_Reg && x._2 == liveSourceLine.route_ID && x._3 == liveSourceLine.direction_ID && x._4 == liveSourceLine.stop_Code)) {
      inputsReceivedCache = inputsReceivedCache :+(liveSourceLine.vehicle_Reg, liveSourceLine.route_ID, liveSourceLine.direction_ID, liveSourceLine.stop_Code, System.currentTimeMillis())
      inputsReceivedCache = inputsReceivedCache.filter(x => x._5 > (System.currentTimeMillis() - CACHE_HOLD_FOR_TIME))

      val vehicleReg = liveSourceLine.vehicle_Reg
      if (liveActors.contains(vehicleReg)) {
        liveActors(vehicleReg)._1 ! liveSourceLine
      } else {

        val newActor: ActorRef = actorSystem.actorOf(Props(new VehicleActor(vehicleReg)), vehicleReg)
        liveActors = liveActors + (vehicleReg ->(newActor, System.currentTimeMillis()))
        newActor ! liveSourceLine //Start it off
      }
    }
    // }
  }

  def getNumberLiveActors = liveActors.size

  def updateLiveActorTimestamp(reg: String) = {
    liveActors = liveActors + (reg ->(liveActors(reg)._1, System.currentTimeMillis()))
    //if (!cleaningInProgress) cleanUpLiveActorsList
  }

  def cleanUpLiveActorsList = {
    this.synchronized {
      cleaningInProgress = true
      val actorsToKill = liveActors.filter(x => x._2._2 < (System.currentTimeMillis() - IDLE_TIME_UNTIL_ACTOR_KILLED))
      actorsToKill.foreach(x => {
        x._2._1 ! PoisonPill //Kill actor
        enqueue(x._1, 0, Array()) //Send kill to stream Queue
        liveActors = liveActors - x._1
        println("ActorKilled: " + x._1)
      })
      cleaningInProgress = false
    }
  }

  def getStream = stream.toStream

  def enqueue(vehicle_ID: String, arrivalTimeAtNextStop: Long, latLngArray: Array[(String, String)]) = stream.enqueue((vehicle_ID, arrivalTimeAtNextStop.toString, latLngArray))

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




