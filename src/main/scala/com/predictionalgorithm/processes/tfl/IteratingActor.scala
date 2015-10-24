package com.predictionalgorithm.processes.tfl

import akka.actor.Actor
import com.predictionalgorithm.datasource.{HttpDataStreamImpl, SourceIterator}
import com.predictionalgorithm.datasource.tfl.{TFLDataSourceImpl, TFLSourceLineFormatterImpl}
import com.typesafe.scalalogging.{LazyLogging, Logger}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Await, Future}
import ExecutionContext.Implicits.global

/**
 * Actor that iterates over live stream sending lines to be processed. On crash, the supervisor strategy restarts it
 */
class IteratingActor extends Actor with LazyLogging {

  // Iterating pattern for this actor based on code snippet posted on StackOverflow
  //http://stackoverflow.com/questions/5626285/pattern-for-interruptible-loops-using-actors
  override def receive: Receive = inactive // Start out as inactive

  def inactive: Receive = { // This is the behavior when inactive
    case Start =>
      val x = TFLIterateOverArrivalStreamSupervisor.getSourceIterator
      logger.info("Iterating Actor becoming active")
      context.become(active(x))

  }

  def active(it: Iterator[String]): Receive = { // This is the behavior when it's active
    case Stop =>
      context.become(inactive)
      TFLIterateOverArrivalStreamSupervisor.closeDataStream
      logger.info("Closing data stream")
    case Next =>
        val lineFuture = Future(TFLSourceLineFormatterImpl(it.next()))
        val line = Await.result(lineFuture, 10 seconds)
        TFLProcessSourceLines(line)
        TFLIterateOverArrivalStreamSupervisor.numberProcessed += 1
      TFLIterateOverArrivalStreamSupervisor.numberProcessedSinceRestart += 1
        self ! Next
      }

  override def postRestart(reason: Throwable): Unit = {
    logger.debug("Iterating Actor Restarting")
    self ! Start
    self ! Next
  }


}
