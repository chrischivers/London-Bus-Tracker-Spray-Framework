package com.PredictionAlgorithm.ControlInterface

import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.Processes.TFL.TFLIterateOverArrivalStream


object DataSourceControlInterface extends ControlInterface {

  val streamActor = actorSystem.actorOf(Props[TFLIterateOverArrivalStream], name = "TFLArrivalStream")


  override def start: Unit = {
    streamActor ! "start"

  }

  override def stop: Unit = {
    streamActor ! "stop"
  }

  override def getVariableArray: Array[String] = {
    val numberProcessed = TFLIterateOverArrivalStream.numberProcessed.toString
    Array(numberProcessed)
  }


}
