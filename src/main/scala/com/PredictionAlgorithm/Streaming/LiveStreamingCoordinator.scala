package com.PredictionAlgorithm.Streaming

import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}

import akka.actor.Status.{Failure, Success}
import akka.actor._
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Prediction.{PredictionRequest, KNNPrediction}
import com.PredictionAlgorithm.Spray.FIFOStreamImplementation

import scala.collection.mutable.ListBuffer
import scala.collection.{SortedMap, mutable}
import scala.concurrent.duration._

case class PackagedStreamObject(reg:String, nextArrivalTime: String, decodedPolyLineToNextStop: Array[(String,String)], route_ID: String, direction_ID: Int, towards:String, nextStopID: String, nextStopName: String)

object LiveStreamingCoordinator {

  implicit val actorSystem = ActorSystem("live_streaming")
  @volatile private var streamList:mutable.ListBuffer[FIFOStreamImplementation] = ListBuffer()
  implicit val timeout = 1000

  private var liveActors = Map[String, (ActorRef, Long)]()
  private var inputsReceivedCache: List[(String, String, Int, String, Long)] = List()
  private val CACHE_HOLD_FOR_TIME = 600000
  private val IDLE_TIME_UNTIL_ACTOR_KILLED = 600000
  @volatile private var cleaningInProgress: Boolean = false

   def setObjectPosition(liveSourceLine: TFLSourceLine): Unit = {
    if (liveSourceLine.route_ID == "3") {
    // This checks it is not aready in the cache
    if (!inputsReceivedCache.exists(x => x._1 == liveSourceLine.vehicle_Reg && x._2 == liveSourceLine.route_ID && x._3 == liveSourceLine.direction_ID && x._4 == liveSourceLine.stop_Code)) {
      inputsReceivedCache = inputsReceivedCache :+(liveSourceLine.vehicle_Reg, liveSourceLine.route_ID, liveSourceLine.direction_ID, liveSourceLine.stop_Code, System.currentTimeMillis())
      inputsReceivedCache = inputsReceivedCache.filter(x => x._5 > (System.currentTimeMillis() - CACHE_HOLD_FOR_TIME))

      val vehicleReg = liveSourceLine.vehicle_Reg
      if (liveActors.contains(vehicleReg)) {
        liveActors(vehicleReg)._1 ! liveSourceLine
      } else {

        val newActor: ActorRef = actorSystem.actorOf(Props(new VehicleActor(vehicleReg)), vehicleReg)
        this.synchronized {
          liveActors = liveActors + (vehicleReg ->(newActor, System.currentTimeMillis()))
        }
        newActor ! liveSourceLine //Start it off
      }
    }
     }
  }

  def registerNewStream(streamImpl: FIFOStreamImplementation): Unit = {
    println("new stream registered")
    streamList += streamImpl
  }

  def getNumberLiveActors = liveActors.size

  def updateLiveActorTimestamp(reg: String) = {
    this.synchronized {
      liveActors = liveActors + (reg ->(liveActors(reg)._1, System.currentTimeMillis()))
    }
    //if (!cleaningInProgress) cleanUpLiveActorsList
  }

  def cleanUpLiveActorsList = {
    this.synchronized {
      cleaningInProgress = true
      val actorsToKill = liveActors.filter(x => x._2._2 < (System.currentTimeMillis() - IDLE_TIME_UNTIL_ACTOR_KILLED))
      actorsToKill.foreach(x => {
        x._2._1 ! PoisonPill //Kill actor
        enqueue(new PackagedStreamObject("","",Array(),"",0,"","","")) //Send kill to stream Queue
        liveActors = liveActors - x._1
        println("ActorKilled: " + x._1)
      })
      cleaningInProgress = false
    }
  }

  def enqueue(pso: PackagedStreamObject) =  {
    streamList.foreach(x=> x.enqueue(pso))
  }

}





