package com.PredictionAlgorithm.Streaming

import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}

import akka.actor.Status.{Failure, Success}
import akka.actor._
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Prediction.{PredictionRequest, KNNPrediction}
import com.PredictionAlgorithm.Streaming.LiveStreamingCoordinator._

import scala.collection.mutable.ListBuffer
import scala.collection.{SortedMap, mutable}
import scala.concurrent.duration._

// Marker movement Data is Lat, Lng, Rotation To Here, Proportional Distance To Here, Label Position To Here Lat, Label Position To Here Lng
case class PackagedStreamObject(reg: String, nextArrivalTime: String, markerMovementData: Array[(String, String, String, String, String, String)], route_ID: String, direction_ID: Int, towards: String, nextStopID: String, nextStopName: String)
case class KillMessage(vehicleID:String, routeID:String)

object LiveStreamingCoordinator extends LiveStreamingCoordinatorInterface {

  // private var inputsReceivedCache: List[(String, String, Int, String, Long)] = List()

  override def processSourceLine(liveSourceLine: TFLSourceLine): Unit = {
    // This checks it is not aready in the cache
    //  if (!inputsReceivedCache.exists(x => x._1 == liveSourceLine.vehicle_Reg && x._2 == liveSourceLine.route_ID && x._3 == liveSourceLine.direction_ID && x._4 == liveSourceLine.stop_Code)) {
    //    inputsReceivedCache = inputsReceivedCache :+(liveSourceLine.vehicle_Reg, liveSourceLine.route_ID, liveSourceLine.direction_ID, liveSourceLine.stop_Code, System.currentTimeMillis())
    //   inputsReceivedCache = inputsReceivedCache.filter(x => x._5 > (System.currentTimeMillis() - CACHE_HOLD_FOR_TIME))

    vehicleSupervisor ! liveSourceLine
  }

  def killActor(km: KillMessage) = {
    vehicleSupervisor ! km
  }
}

class LiveVehicleSupervisor extends Actor {

  var TIME_OF_LAST_CLEANUP:Long = 0
  val TIME_BETWEEN_CLEANUPS = 60000
  
  var liveActors = mutable.Map[String, (ActorRef, String, Long)]()

  def receive = {
    case liveSourceLine: TFLSourceLine => processLine(liveSourceLine)
    case km: KillMessage => killActor(km)
    case actor: Terminated =>  liveActors.remove(actor.getActor.path.name)
  }

  def processLine(liveSourceLine:TFLSourceLine) = {

    val vehicle_Reg = liveSourceLine.vehicle_Reg
    if (liveActors.contains(vehicle_Reg)) {
      updateLiveActorTimestamp(vehicle_Reg,liveSourceLine.route_ID,System.currentTimeMillis())
      liveActors(vehicle_Reg)._1 ! liveSourceLine
    } else {
      val newVehicleActor = vehicleSystem.actorOf(Props(new VehicleActor(vehicle_Reg)), vehicle_Reg)
      liveActors += (vehicle_Reg ->(newVehicleActor, liveSourceLine.route_ID, System.currentTimeMillis()))
      newVehicleActor ! liveSourceLine
      context.watch(newVehicleActor)
    }
    numberLiveActors = liveActors.size //Update variable
  }

  def updateLiveActorTimestamp(reg: String, routeID: String, timeStamp: Long) = {
    this.synchronized {
      val currentValue = liveActors.get(reg)
      if (currentValue.isDefined) liveActors += (reg -> (currentValue.get._1, routeID, timeStamp))
      if (System.currentTimeMillis() - TIME_OF_LAST_CLEANUP > TIME_BETWEEN_CLEANUPS) cleanUpLiveActorsList
    }

  }

  def cleanUpLiveActorsList() = {
    TIME_OF_LAST_CLEANUP = System.currentTimeMillis()
    val cutOffThreshold = System.currentTimeMillis() - IDLE_TIME_UNTIL_ACTOR_KILLED
    val actorsToKill = liveActors.filter(x => x._2._3 < cutOffThreshold)
    actorsToKill.foreach(x => {
      killActor(new KillMessage(x._1, x._2._2))//Kill actor
    })
  }

  def killActor(km:KillMessage) = {
    val value = liveActors.get(km.vehicleID)
    if (value.isDefined) value.get._1 ! PoisonPill
    enqueue(new PackagedStreamObject(km.vehicleID,"kill",Array(),km.routeID ,0,"0","0","0")) //Send kill to stream Queue
  }

}
