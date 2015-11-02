package com.predictionalgorithm.streaming

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import com.predictionalgorithm.Main
import com.predictionalgorithm.controlinterface.LiveStreamControlInterface
import com.predictionalgorithm.datasource.tfl.TFLSourceLineImpl
import com.predictionalgorithm.processes.tfl.TFLProcessSourceLines
import com.predictionalgorithm.spray.WebServer.{WebSocketServer, PushToChildren}
import com.predictionalgorithm.streaming.LiveStreamingCoordinatorImpl._
import spray.can.Http
import spray.can.server.UHttp


trait LiveStreamingCoordinator {

  val actorVehicleSystem = ActorSystem("VehicleSystem")
  val vehicleSupervisor = actorVehicleSystem.actorOf(Props[LiveVehicleSupervisor], "VehicleSupervisor")

  implicit val actorServerSystem =  ActorSystem("WebServeSystem")
  val server = actorServerSystem.actorOf(WebSocketServer.props(), "websocket")

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

  def stop(): Unit = {
    IO(UHttp) ! Http.Unbind
    TFLProcessSourceLines.setLiveStreamCollection(false)
  }

  /**
   * Binds server
   */
  def start(): Unit = {
   // IO(UHttp) ! Http.Bind(server, interface = "0.0.0.0", port = 80)
    IO(UHttp) ! Http.Bind(server, interface = "0.0.0.0", port = 8080)
    TFLProcessSourceLines.setLiveStreamCollection(true)
  }


}
