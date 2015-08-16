package com.PredictionAlgorithm.ControlInterface

import akka.actor.ActorSystem
import akka.io.IO
import com.PredictionAlgorithm.Prediction.{KNNPrediction, PredictionInterface}
import com.PredictionAlgorithm.Processes.TFL.{TFLProcessSourceLines, TFLIterateOverArrivalStream}
import com.PredictionAlgorithm.Spray.SimpleServer.WebSocketServer
import com.PredictionAlgorithm.Streaming.{PackagedStreamObject, LiveStreamResult, LiveStreamingCoordinator, LiveStream}
import spray.can.Http
import spray.can.server.UHttp

object LiveStreamControlInterface extends StartStopControlInterface {

  implicit val system = ActorSystem("websocket")
  val server = system.actorOf(WebSocketServer.props(), "websocket")


  override def getVariableArray: Array[String] = {
    val numberLiveActors  = LiveStreamingCoordinator.getNumberLiveActors.toString
    Array(numberLiveActors)
  }

  override def stop: Unit = {
    IO(UHttp) ! Http.Unbind
    TFLProcessSourceLines.setLiveStreamCollection(false)
  }

  override def start: Unit = {
    IO(UHttp) ! Http.Bind(server, interface = "0.0.0.0", port = 8080)
    TFLProcessSourceLines.setLiveStreamCollection(true)
  }

}
