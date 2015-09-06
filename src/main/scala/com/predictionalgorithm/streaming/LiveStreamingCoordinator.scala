package com.predictionalgorithm.streaming

import akka.actor.{Props, ActorSystem}
import com.predictionalgorithm.controlinterface.LiveStreamControlInterface
import com.predictionalgorithm.datasource.tfl.TFLSourceLineImpl
import com.predictionalgorithm.spray.WebServer.PushToChildren
import com.predictionalgorithm.streaming.LiveStreamingCoordinatorImpl._


trait LiveStreamingCoordinator {
  val server = LiveStreamControlInterface.server

  val vehicleSystem = ActorSystem("vehicles")
  val vehicleSupervisor = vehicleSystem.actorOf(Props[LiveVehicleSupervisor], "VehicleSupervisor")
  @volatile var numberLiveActors = 0
  @volatile var numberLiveChildren = 0

  //implicit val timeout = 1000
  val CACHE_HOLD_FOR_TIME:Int
  val IDLE_TIME_UNTIL_ACTOR_KILLED:Int


  def processSourceLine(liveSourceLine: TFLSourceLineImpl) = vehicleSupervisor ! liveSourceLine

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
