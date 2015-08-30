package com.predictionalgorithm.processes


import akka.actor.Actor

trait ProcessingInterface extends Actor{
  def start()
  def stop()

  def receive = {
    case "start" => start()
    case "stop" => stop()
  }
}
