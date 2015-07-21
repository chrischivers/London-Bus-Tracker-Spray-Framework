package com.PredictionAlgorithm.Processes


import akka.actor.Actor
import com.mongodb.casbah.MongoCollection

import scala.concurrent.Future


trait ProcessingInterface extends Actor{
  def start
  def stop

  def receive = {
    case "start" => start
    case "stop" => stop
  }
}

trait ProcessLinesInterface extends ProcessingInterface{

  lazy val dbCollection: MongoCollection = getDBCollection

  def getDBCollection:MongoCollection

}

