package com.PredictionAlgorithm.Spray

import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.Processes.StartMessage
import com.PredictionAlgorithm.Processes.TFL.TFLProcessArrivalStream
import spray.routing.SimpleRoutingApp

// Code based on example provided by Adam Warski
// https://www.youtube.com/watch?v=XPuOlpWEvmw

object ScalaBay extends App with SimpleRoutingApp{

  implicit val actorSystem = ActorSystem()

  startServer(interface= "localhost", port = 8080) {
    get {
      path("hello") {
        complete {
          "Welcome to Silicon Valley!"
        }
      }

    } ~
    get {
      path("start") {
        complete {
          val asActor = actorSystem.actorOf(Props[TFLProcessArrivalStream], name = "TFLArrivalStream")
          asActor ! StartMessage
          "Started"
        }
      }

    }

  }

}
