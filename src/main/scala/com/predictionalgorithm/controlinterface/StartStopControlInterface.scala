package com.predictionalgorithm.controlinterface

import akka.actor.ActorSystem
import com.predictionalgorithm.Main

/**
 * Interface for User Interfaces that use a Start/Stop Button
 */
trait StartStopControlInterface  {

  def getVariableArray:Array[String]
 // def checkAndSendForEmailAlerting(variableArray: Array[String])
  def start()
  def stop()


}
