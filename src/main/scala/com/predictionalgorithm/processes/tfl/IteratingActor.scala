package com.predictionalgorithm.processes.tfl

import akka.actor.Actor
import com.predictionalgorithm.datasource.{HttpDataStreamImpl, SourceIterator}
import com.predictionalgorithm.datasource.tfl.{TFLDataSourceImpl, TFLSourceLineFormatterImpl}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Await, Future}
import ExecutionContext.Implicits.global

/**
 * Actor that iterates over live stream sending lines to be processed. On crash, the supervisor strategy restarts it
 */
class IteratingActor extends Actor {
  var it = getSourceIterator

  // Iterating pattern for this actor based on code snippet posted on StackOverflow
  //http://stackoverflow.com/questions/5626285/pattern-for-interruptible-loops-using-actors
  override def receive: Receive = inactive // Start out as inactive

  def inactive: Receive = { // This is the behavior when inactive
    case Start =>
      context.become(active)
  }

  def active: Receive = { // This is the behavior when it's active
    case Stop =>
      context.become(inactive)
    case Next =>
        val lineFuture = Future(TFLSourceLineFormatterImpl(it.next()))
        val line = Await.result(lineFuture, 10 seconds)
        TFLProcessSourceLines(line)
        TFLIterateOverArrivalStreamSupervisor.numberProcessed += 1
      TFLIterateOverArrivalStreamSupervisor.numberProcessedSinceRestart += 1
        self ! Next
      }

  override def postRestart(reason: Throwable): Unit = {
    self ! Start
    self ! Next
  }

  def getSourceIterator: Iterator[String] = {
    while (true) {
      try {
        println("Getting HTTP Source")
        return new SourceIterator(new HttpDataStreamImpl(TFLDataSourceImpl)).iterator
      } catch {
        case e: Exception => Thread.sleep(5000)
      }
    }
    throw new IllegalStateException
  }
}
