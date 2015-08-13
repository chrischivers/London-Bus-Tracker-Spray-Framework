package com.PredictionAlgorithm.Streaming

import akka.actor.{PoisonPill, ActorSystem, ActorRef}
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Spray.FIFOStreamImplementation

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class livePositionData(routeID: String, directionID: Int, pointSequence: Int, nextStopID: String, arrivalTime: Long, firstLast:Option[String])

trait LiveStreamingCoordinatorInterface {

  var liveActors = Map[String, (ActorRef, String, Long)]()
  implicit val actorSystem = ActorSystem("live_streaming")
  @volatile var streamList:mutable.ListBuffer[FIFOStreamImplementation] = ListBuffer()
  implicit val timeout = 1000
  val CACHE_HOLD_FOR_TIME = 600000
  val IDLE_TIME_UNTIL_ACTOR_KILLED = 600000
  var TIME_OF_LAST_CLEANUP:Long = 0
  val TIME_BETWEEN_CLEANUPS = 60000
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

  def updateLiveActorTimestamp(reg: String, routeID: String, timeStamp: Long) = {
    this.synchronized {
      val currentValue = liveActors.get(reg)
      if (currentValue.isDefined) liveActors = liveActors + (reg -> (currentValue.get._1, routeID, timeStamp))
      if (!cleaningInProgress && System.currentTimeMillis() - TIME_OF_LAST_CLEANUP > TIME_BETWEEN_CLEANUPS) cleanUpLiveActorsList
    }

  }
  def enqueue(pso: PackagedStreamObject) =  {
    streamList.foreach(x=> x.enqueue(pso))
  }

  def cleanUpLiveActorsList = {
      cleaningInProgress = true
      TIME_OF_LAST_CLEANUP = System.currentTimeMillis()
      val cutOffThreshold = System.currentTimeMillis() - IDLE_TIME_UNTIL_ACTOR_KILLED
      val actorsToKill = liveActors.filter(x => x._2._3 < cutOffThreshold)
      actorsToKill.foreach(x => {
        killActor(x._1, x._2._2)//Kill actor
      })
      cleaningInProgress = false
  }

  def killActor(reg: String, routeID: String ) = {
    val value = liveActors.get(reg)
      if (value.isDefined) value.get._1 ! PoisonPill
      enqueue(new PackagedStreamObject(reg,"kill",Array(),routeID ,0,"0","0","0")) //Send kill to stream Queue
      liveActors = liveActors - reg
  }

}
