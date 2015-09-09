package com.predictionalgorithm.controlinterface

import akka.actor.ActorSystem
import akka.io.IO
import com.predictionalgorithm.Main
import com.predictionalgorithm.processes.tfl.TFLProcessSourceLines
import com.predictionalgorithm.spray.WebServer
import com.predictionalgorithm.spray.WebServer.WebSocketServer
import com.predictionalgorithm.streaming.LiveStreamingCoordinatorImpl
import spray.can.Http
import spray.can.server.UHttp

/**
 * User Control Interface for  Live Stream Control
 */
object LiveStreamControlInterface extends StartStopControlInterface {

  override def getVariableArray: Array[String] = {
    val numberLiveActors  = LiveStreamingCoordinatorImpl.getNumberLiveActors.toString
    val numberLiveChildren = LiveStreamingCoordinatorImpl.getNumberLiveChildren.toString
    Array(numberLiveActors, numberLiveChildren)
  }

  override def start(): Unit = {
    LiveStreamingCoordinatorImpl.start()
  }

  override def stop(): Unit = {
    LiveStreamingCoordinatorImpl.stop()
  }




}
