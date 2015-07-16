package com.PredictionAlgorithm.ControlInterface

import akka.actor.ActorSystem

/**
 * Created by chrischivers on 16/07/15.
 */
trait ControlInterface {

  implicit val actorSystem = ActorSystem()

  def start
  def stop
  def getVariableArray:Array[String]

}
