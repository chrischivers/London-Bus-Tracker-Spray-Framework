package com.PredictionAlgorithm.ControlInterface

import akka.actor.ActorSystem

/**
 * Created by chrischivers on 16/07/15.
 */
trait StartStopControlInterface  {
  implicit val actorSystem = ActorSystem("ControlInterfaceActorSystem")

  def getVariableArray:Array[String]
  def start
  def stop


}
