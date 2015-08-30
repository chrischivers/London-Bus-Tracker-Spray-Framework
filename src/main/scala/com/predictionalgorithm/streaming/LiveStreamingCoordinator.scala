package com.predictionalgorithm.streaming

import akka.actor.{Props, ActorSystem}
import com.predictionalgorithm.controlinterface.LiveStreamControlInterface
import com.predictionalgorithm.datasource.tfl.TFLSourceLineImpl
import com.predictionalgorithm.spray.WebServer.PushToChildren


trait LiveStreamingCoordinator {
  val server = LiveStreamControlInterface.server

  val vehicleSystem = ActorSystem("vehicles")
  val vehicleSupervisor = vehicleSystem.actorOf(Props[LiveVehicleSupervisor], "VehicleSupervisor")
  var numberLiveActors = 0
  var numberLiveChildren = 0

  implicit val timeout = 1000
  val CACHE_HOLD_FOR_TIME = 600000
  val IDLE_TIME_UNTIL_ACTOR_KILLED = 600000


  def processSourceLine(liveSourceLine: TFLSourceLineImpl)

  def getNumberLiveActors = numberLiveActors

  def getNumberLiveChildren = numberLiveChildren

  /**
   * Pushes a packaged stream object to clients
   * @param pso The packaged stream objects
   */
  def pushToClients(pso: PackagedStreamObject) =  {
    server ! PushToChildren(pso)
  }


}
