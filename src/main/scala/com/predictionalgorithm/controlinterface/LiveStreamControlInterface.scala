package com.predictionalgorithm.controlinterface

import akka.actor.ActorSystem
import akka.io.IO
import com.predictionalgorithm.processes.tfl.TFLProcessSourceLines
import com.predictionalgorithm.spray.WebServer.WebSocketServer
import com.predictionalgorithm.streaming.LiveStreamingCoordinator
import spray.can.Http
import spray.can.server.UHttp

/**
 * User Control Interface for  Live Stream Control
 */
object LiveStreamControlInterface extends StartStopControlInterface {

  implicit val system = ActorSystem("websocket")
  val server = system.actorOf(WebSocketServer.props(), "websocket")


  override def getVariableArray: Array[String] = {
    val numberLiveActors  = LiveStreamingCoordinator.getNumberLiveActors.toString
    val numberLiveChildren = LiveStreamingCoordinator.getNumberLiveChildren.toString
    Array(numberLiveActors, numberLiveChildren)
  }

  override def stop(): Unit = {
    IO(UHttp) ! Http.Unbind
    TFLProcessSourceLines.setLiveStreamCollection(false)
  }

  /**
   * Binds server
   */
  override def start(): Unit = {
    IO(UHttp) ! Http.Bind(server, interface = "0.0.0.0", port = 8080)
    TFLProcessSourceLines.setLiveStreamCollection(true)
  }

}
