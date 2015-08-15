package com.PredictionAlgorithm.Streaming

import akka.actor.{Props, PoisonPill, ActorSystem, ActorRef}
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Main
import com.PredictionAlgorithm.Spray.SimpleServer
import com.PredictionAlgorithm.Spray.SimpleServer.{PushToChildren, WebSocketServer}
import com.PredictionAlgorithm.Streaming.LiveStreamingCoordinator._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class livePositionData(routeID: String, directionID: Int, pointSequence: Int, nextStopID: String, arrivalTime: Long, firstLast:Option[String])

trait LiveStreamingCoordinatorInterface {
  val server = Main.server

  val vehicleSystem = ActorSystem("vehicles")
  val watcherActor = vehicleSystem.actorOf(Props[LiveVehicleSupervisor], "VehicleSupervisor")
  var numberLiveActors = 0

  implicit val timeout = 1000
  val CACHE_HOLD_FOR_TIME = 600000
  val IDLE_TIME_UNTIL_ACTOR_KILLED = 600000


  def setObjectPosition(liveSourceLine: TFLSourceLine)


  def getNumberLiveActors = numberLiveActors


  def enqueue(pso: PackagedStreamObject) =  {
    server ! PushToChildren(pso)
  }


}
