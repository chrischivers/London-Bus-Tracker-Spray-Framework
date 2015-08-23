package com.PredictionAlgorithm.ControlInterface

import akka.actor.ActorSystem

/**
 * Interface for User Interfaces that use a Start/Stop Button
 */
trait StartStopControlInterface  {
  implicit val actorSystem = ActorSystem("ControlInterfaceActorSystem")

  def getVariableArray:Array[String]
  def start()
  def stop()


}
