package com.PredictionAlgorithm.Processes.TFL


import java.net.UnknownHostException

import akka.actor.{Props, Actor}
import com.PredictionAlgorithm.ControlInterface.DataReadProcessStoreControlInterface._
import com.PredictionAlgorithm.DataSource.TFL.{TFLSourceLineFormatter, TFLDataSource, TFLSourceLine}
import com.PredictionAlgorithm.DataSource._
import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Processes.{IterateOverArrivalStreamInterface}

import scala.util.{Failure, Success, Try}


class TFLIterateOverArrivalStream extends IterateOverArrivalStreamInterface {

  val iteratingActor = actorSystem.actorOf(Props(new IteratingActor(getSourceIterator)), name = "IteratorStream")


  def getSourceIterator =
    Try(new SourceIterator(new HttpDataStream(TFLDataSource))) match {
      case Success(src) => src.iterator
      case Failure(fail) => throw new IllegalStateException("Cannot get Source Iterator")
    }


  override def start = {
      iteratingActor ! "start"
      iteratingActor ! "next"
  }

  override def stop = {
    iteratingActor ! "stop"
  }
}


object TFLIterateOverArrivalStream {
  @volatile var numberProcessed:Long = 0

}

//TODO consider abstracting this to an interface
class IteratingActor(it: Iterator[String]) extends Actor {
  override def receive: Receive = inactive // Start out as inactive

  def inactive: Receive = { // This is the behavior when inactive
    case "start" =>
      context.become(active)
  }

  def active: Receive = { // This is the behavior when it's active
    case "stop" =>
      context.become(inactive)
    case "next" =>
      val line = TFLSourceLineFormatter(it.next())
      TFLProcessSourceLines(line)
      TFLIterateOverArrivalStream.numberProcessed += 1
      self ! "next"
  }
}