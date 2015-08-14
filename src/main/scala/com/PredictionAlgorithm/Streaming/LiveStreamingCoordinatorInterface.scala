package com.PredictionAlgorithm.Streaming

import akka.actor.{Props, PoisonPill, ActorSystem, ActorRef}
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Spray.FIFOStreamImplementation
import com.PredictionAlgorithm.Streaming.LiveStreamingCoordinator._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class livePositionData(routeID: String, directionID: Int, pointSequence: Int, nextStopID: String, arrivalTime: Long, firstLast:Option[String])

trait LiveStreamingCoordinatorInterface {

  implicit val actorSystem = ActorSystem("live_streaming")
  val watcherActor = actorSystem.actorOf(Props[LiveVehicleSupervisor], "VehicleSupervisor")
  var numberLiveActors = 0

  @volatile var streamList:mutable.ListBuffer[FIFOStreamImplementation] = ListBuffer()
  implicit val timeout = 1000
  val CACHE_HOLD_FOR_TIME = 600000
  val IDLE_TIME_UNTIL_ACTOR_KILLED = 600000


  def setObjectPosition(liveSourceLine: TFLSourceLine)

  def registerNewStream(streamImpl: FIFOStreamImplementation): Unit = {
    println("new stream registered (" + streamImpl + ")")
    streamList += streamImpl
  }

  def deregisterStream(streamImpl: FIFOStreamImplementation) = {
    println("stream deregistered (" + streamImpl + ")")
    streamList -= streamImpl
  }


  def getNumberLiveActors = numberLiveActors


  def enqueue(pso: PackagedStreamObject) =  {
    streamList.foreach(x=> x.enqueue(pso))
  }


}
