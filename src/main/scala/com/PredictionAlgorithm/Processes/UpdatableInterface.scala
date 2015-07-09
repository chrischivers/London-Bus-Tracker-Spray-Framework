package com.PredictionAlgorithm.Processes


import akka.actor.Actor
import com.mongodb.casbah.MongoCollection

import scala.concurrent.Future

case object StartMessage
case object StopMessage

sealed trait ProcessingInterface extends Actor{
  var numberProccessed: Int
  def start

  def receive = {
    case StartMessage => start
    case StopMessage => context.stop(self)
  }
}

trait ProcessArrivalStreamInterface extends ProcessingInterface{

  lazy val dbCollection: MongoCollection = getDBCollection

  def getDBCollection:MongoCollection

  def getSourceIterator:Iterator[String]

  def startIterating(src: Iterator[String]):Unit



}
