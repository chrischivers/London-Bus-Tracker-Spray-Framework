package com.PredictionAlgorithm.Processes


import akka.actor.Actor
import com.mongodb.casbah.MongoCollection

import scala.concurrent.Future

case object StartMessage
case object StopMessage

sealed trait ProcessingInterface extends Actor{
  var numberProccessed: Int = 0
  def start

  def receive = {
    case StartMessage => start
    case StopMessage => context.stop(self)
  }
}

trait IterateOverArrivalStreamInterface extends ProcessingInterface{


  def getSourceIterator:Iterator[String]

  def startIterating(src: Iterator[String]):Unit

}

trait ProcessLinesInterface extends ProcessingInterface{

  lazy val dbCollection: MongoCollection = getDBCollection

  def getDBCollection:MongoCollection

}

