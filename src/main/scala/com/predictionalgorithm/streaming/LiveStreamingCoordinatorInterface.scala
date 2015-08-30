package com.predictionalgorithm.streaming

import akka.actor.{Props, ActorSystem}
import com.predictionalgorithm.controlinterface.LiveStreamControlInterface
import com.predictionalgorithm.datasource.tfl.TFLSourceLine
import com.predictionalgorithm.spray.WebServer.PushToChildren


case class livePositionData(routeID: String, directionID: Int, pointSequence: Int, nextStopID: String, arrivalTime: Long, firstLast:Option[String])

trait LiveStreamingCoordinatorInterface {
  val server = LiveStreamControlInterface.server

  val vehicleSystem = ActorSystem("vehicles")
  val vehicleSupervisor = vehicleSystem.actorOf(Props[LiveVehicleSupervisor], "VehicleSupervisor")
  var numberLiveActors = 0
  var numberLiveChildren = 0

  implicit val timeout = 1000
  val CACHE_HOLD_FOR_TIME = 600000
  val IDLE_TIME_UNTIL_ACTOR_KILLED = 600000


  def processSourceLine(liveSourceLine: TFLSourceLine)


  def getNumberLiveActors = numberLiveActors

  def getNumberLiveChildren = numberLiveChildren


  def enqueue(pso: PackagedStreamObject) =  {
    server ! PushToChildren(pso)
  }


}
