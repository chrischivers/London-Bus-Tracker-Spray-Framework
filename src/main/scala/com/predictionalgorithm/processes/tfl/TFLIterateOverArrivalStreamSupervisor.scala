package com.predictionalgorithm.processes.tfl


import java.util.concurrent.TimeoutException

import akka.actor.SupervisorStrategy._
import akka.actor.{ OneForOneStrategy, Props, Actor}

import com.predictionalgorithm.datasource._
import com.predictionalgorithm.processes.ProcessingInterface


final case class Start()
final case class Stop()
final case class Next()

class TFLIterateOverArrivalStreamSupervisor extends Actor {

  val iteratingActor = context.actorOf(Props[IteratingActor])

  def receive = {
    case  Start=>
      iteratingActor ! Start
      iteratingActor ! Next
    case Stop => iteratingActor ! Stop
  }

  /**
   * Supervisers the Actor, ensuring that it restarts if it ctrashes
   */
  override val supervisorStrategy =
    OneForOneStrategy(loggingEnabled = false) {
      case e: TimeoutException =>
        println("Incoming Stream TimeOut Exception. Restarting...")
        Thread.sleep(5000)
        TFLIterateOverArrivalStreamSupervisor.numberProcessedSinceRestart = 0
        Restart
      case e: Exception =>
        println("Incoming Stream Exception. Restarting...")
        println(e.printStackTrace())
        Thread.sleep(5000)
        TFLIterateOverArrivalStreamSupervisor.numberProcessedSinceRestart = 0
        Restart
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

}


object TFLIterateOverArrivalStreamSupervisor extends ProcessingInterface{
  @volatile var numberProcessed:Long = 0
  @volatile var numberProcessedSinceRestart:Long = 0

  val supervisor = actorProcessingSystem.actorOf(Props[TFLIterateOverArrivalStreamSupervisor], name = "TFLIterateOverArrivalStreamSupervisor")


  override def start(): Unit = {
    supervisor ! Start

  }

  override def stop(): Unit = {
    supervisor ! Stop
  }

}

