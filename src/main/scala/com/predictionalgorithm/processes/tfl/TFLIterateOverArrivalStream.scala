package com.predictionalgorithm.processes.tfl


import akka.actor.SupervisorStrategy._
import akka.actor.{ OneForOneStrategy, Props, Actor}

import com.predictionalgorithm.datasource.tfl.{TFLSourceLineFormatterImpl, TFLDataSourceImpl}
import com.predictionalgorithm.datasource._
import com.predictionalgorithm.processes.ProcessingInterface
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import ExecutionContext.Implicits.global


class TFLIterateOverArrivalStream extends ProcessingInterface {

  val iteratingActor = context.actorOf(Props[IteratingActor])

  override def start() = {
      iteratingActor ! "start"
      iteratingActor ! "next"
  }

  override def stop() = {
    iteratingActor ! "stop"
  }

  /**
   * Supervisers the Actor, ensuring that it restarts if it ctrashes
   */
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute, loggingEnabled = false) {
      case _: Exception =>
        println("actor exception. Restarting...")
        Thread.sleep(5000)
        Restart
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

}


object TFLIterateOverArrivalStream {
  @volatile var numberProcessed:Long = 0

}

/**
 * Actor that iterates over live stream sending lines to be processed. On crash, the supervisor strategy restarts it
 */
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
        val lineFuture = Future(TFLSourceLineFormatterImpl(it.next()))
        val line = Await.result(lineFuture, 3 seconds)
        TFLProcessSourceLines(line)
        TFLIterateOverArrivalStream.numberProcessed += 1
        self ! "next"
      }

  override def postRestart(reason: Throwable): Unit = {
    self ! "start"
    self ! "next"
  }

  def getSourceIterator = new SourceIterator(new HttpDataStreamImpl(TFLDataSourceImpl)).iterator
}