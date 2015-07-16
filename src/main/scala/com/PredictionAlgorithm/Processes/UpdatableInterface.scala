package com.PredictionAlgorithm.Processes


import akka.actor.Actor
import com.mongodb.casbah.MongoCollection

import scala.concurrent.Future


sealed trait ProcessingInterface extends Actor{
  var numberProcessed: Int = 0
  def start
  def stop

  def receive = {
    case "start" => start
    case "stop" => stop
  }
}

trait IterateOverArrivalStreamInterface extends ProcessingInterface{


 // def getSourceIterator:Iterator[String]

 // def startIterating(src: Iterator[String]):Unit

}

trait ProcessLinesInterface extends ProcessingInterface{

  lazy val dbCollection: MongoCollection = getDBCollection

  def getDBCollection:MongoCollection

}

