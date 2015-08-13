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

// Marker movement Data is Lat, Lng, Rotation To Here, Proportional Distance To Here, Label Position To Here Lat, Label Position To Here Lng
case class PackagedStreamObject(reg:String, nextArrivalTime: String, markerMovementData: Array[(String,String,String, String, String, String)], route_ID: String, direction_ID: Int, towards:String, nextStopID: String, nextStopName: String)

object LiveStreamingCoordinator extends LiveStreamingCoordinatorInterface {

 // private var inputsReceivedCache: List[(String, String, Int, String, Long)] = List()


  override def setObjectPosition(liveSourceLine: TFLSourceLine): Unit = {
      // This checks it is not aready in the cache
    //  if (!inputsReceivedCache.exists(x => x._1 == liveSourceLine.vehicle_Reg && x._2 == liveSourceLine.route_ID && x._3 == liveSourceLine.direction_ID && x._4 == liveSourceLine.stop_Code)) {
    //    inputsReceivedCache = inputsReceivedCache :+(liveSourceLine.vehicle_Reg, liveSourceLine.route_ID, liveSourceLine.direction_ID, liveSourceLine.stop_Code, System.currentTimeMillis())
     //   inputsReceivedCache = inputsReceivedCache.filter(x => x._5 > (System.currentTimeMillis() - CACHE_HOLD_FOR_TIME))

        val vehicleReg = liveSourceLine.vehicle_Reg
        if (liveActors.contains(vehicleReg)) {
          liveActors(vehicleReg)._1 ! liveSourceLine
        } else {

          val newVehicleActor: ActorRef = actorSystem.actorOf(Props(new VehicleActor(vehicleReg)), vehicleReg)
          this.synchronized {
            liveActors = liveActors + (vehicleReg ->(newVehicleActor, liveSourceLine.route_ID, System.currentTimeMillis()))
          }
          newVehicleActor ! liveSourceLine //Start it off
        }
     // }
  }

}





