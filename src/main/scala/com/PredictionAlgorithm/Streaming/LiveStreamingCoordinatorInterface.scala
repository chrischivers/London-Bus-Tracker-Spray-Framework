package com.PredictionAlgorithm.Streaming

import akka.actor.{ActorSystem, ActorRef}
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Spray.FIFOStreamImplementation

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class livePositionData(routeID: String, directionID: Int, pointSequence: Int, nextStopID: String, arrivalTime: Long, firstLast:Option[String])

trait LiveStreamingCoordinatorInterface {

  var liveActors = Map[String, (ActorRef, Long)]()
  implicit val actorSystem = ActorSystem("live_streaming")
  @volatile var streamList:mutable.ListBuffer[FIFOStreamImplementation] = ListBuffer()
  implicit val timeout = 1000
  val CACHE_HOLD_FOR_TIME = 600000
  val IDLE_TIME_UNTIL_ACTOR_KILLED = 600000
  @volatile private var cleaningInProgress: Boolean = false

  def setObjectPosition(liveSourceLine: TFLSourceLine)

  def registerNewStream(streamImpl: FIFOStreamImplementation): Unit = {
    println("new stream registered (" + streamImpl + ")")
    streamList += streamImpl
  }

  def deregisterStream(streamImpl: FIFOStreamImplementation) = {
    println("stream deregistered (" + streamImpl + ")")
    streamList -= streamImpl
  }


  def getNumberLiveActors = liveActors.size

  def updateLiveActorTimestamp(reg: String) = {
    this.synchronized {
      liveActors = liveActors + (reg ->(liveActors(reg)._1, System.currentTimeMillis()))
    }
    //if (!cleaningInProgress) cleanUpLiveActorsList
  }
  def enqueue(pso: PackagedStreamObject) =  {
    streamList.foreach(x=> x.enqueue(pso))
  }

  def cleanUpLiveActorsList = {
    this.synchronized {
      cleaningInProgress = true
      val actorsToKill = liveActors.filter(x => x._2._2 < (System.currentTimeMillis() - IDLE_TIME_UNTIL_ACTOR_KILLED))
      actorsToKill.foreach(x => {
        x._2._1 ! PoisonPill //Kill actor
        enqueue(new PackagedStreamObject("","",Array(),"",0,"","","")) //Send kill to stream Queue
        liveActors = liveActors - x._1
        println("ActorKilled: " + x._1)
      })
      cleaningInProgress = false
    }
  }

}
