package com.PredictionAlgorithm.Processes.TFL


import java.net.UnknownHostException

import akka.actor.SupervisorStrategy._
import akka.actor.{ActorInitializationException, OneForOneStrategy, Props, Actor}
import com.PredictionAlgorithm.ControlInterface.StreamProcessingControlInterface._
import com.PredictionAlgorithm.DataSource.TFL.{TFLSourceLineFormatter, TFLDataSource, TFLSourceLine}
import com.PredictionAlgorithm.DataSource._
import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Processes.ProcessingInterface
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import scala.util.{Failure, Success, Try}


class TFLIterateOverArrivalStream extends ProcessingInterface {

  val iteratingActor = context.actorOf(Props[IteratingActor])

  override def start = {
      iteratingActor ! "start"
      iteratingActor ! "next"
  }

  override def stop = {
    iteratingActor ! "stop"
  }

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception => {
                    println("actor exception. Restarting...")
                    Thread.sleep(5000)
                    Restart
      }
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

}


object TFLIterateOverArrivalStream {
  @volatile var numberProcessed:Long = 0

}

//TODO consider abstracting this to an interface
class IteratingActor extends Actor {
  lazy val it = getSourceIterator

  // Iterating pattern for this actor based on code snippet posted on StackOverflow
  //http://stackoverflow.com/questions/5626285/pattern-for-interruptible-loops-using-actors


  override def receive: Receive = inactive // Start out as inactive

  def inactive: Receive = { // This is the behavior when inactive
    case "start" =>
      context.become(active)
  }

  def active: Receive = { // This is the behavior when it's active
    case "stop" =>
      context.become(inactive)
    case "next" =>
        val lineFuture = Future(TFLSourceLineFormatter(it.next()))
        val line = Await.result(lineFuture, 3 seconds)
        TFLProcessSourceLines(line)
        TFLIterateOverArrivalStream.numberProcessed += 1
        self ! "next"
      }

  override def postRestart(reason: Throwable): Unit = {
    self ! "start"
    self ! "next"
  }

  def getSourceIterator =
   new SourceIterator(new HttpDataStream(TFLDataSource)).iterator
}