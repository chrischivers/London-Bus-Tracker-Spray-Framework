package com.predictionalgorithm.streaming

import akka.actor.SupervisorStrategy.Escalate
import akka.actor._
import com.predictionalgorithm.datasource.tfl.TFLSourceLineImpl
import com.predictionalgorithm.streaming.LiveStreamingCoordinatorImpl._
import scala.concurrent.duration._

/**
 * Case class for the packaged stream object sent to clients
 * @param reg The vehicle reg
 * @param nextArrivalTime The arrival time at the next point
 * @param markerMovementData An array of marker movement data (Lat, Lng, Rotation To Here, Proportional Distance To Here)
 * @param route_ID The route ID
 * @param direction_ID The direction ID
 * @param towards Towards
 * @param nextStopID The next Stop ID
 * @param nextStopName the next Stop Name
 */
case class PackagedStreamObject(reg: String, nextArrivalTime: String, markerMovementData: Array[(String, String, String, String)], route_ID: String, direction_ID: Int, towards: String, nextStopID: String, nextStopName: String)
case class KillMessage(vehicleID: String, routeID: String)
case class CleanUp()
case class UpdateLiveActorsObj(reg: String, routeID: String, timeStamp: Long)

object LiveStreamingCoordinatorImpl extends LiveStreamingCoordinator {

  override def processSourceLine(liveSourceLine: TFLSourceLineImpl): Unit = vehicleSupervisor ! liveSourceLine

  def killActor(km: KillMessage) = vehicleSupervisor ! km
}

/**
 * The Supervising Actor
 */
class LiveVehicleSupervisor extends Actor {

  var TIME_OF_LAST_CLEANUP = System.currentTimeMillis()
  val TIME_BETWEEN_CLEANUPS = 60000

  /**
   * The record of live actors. A Map of the VehicleID to the ActorRef, The Route, and the time last updated
   */
  @volatile private var liveActors = Map[String, (ActorRef, String, Long)]()

  override def receive = {
    case liveSourceLine: TFLSourceLineImpl => processLine(liveSourceLine)
    case km: KillMessage => killActor(km)
    case actor: Terminated => liveActors -= actor.getActor.path.name
    case CleanUp => cleanUpLiveActorsList()
    case obj:UpdateLiveActorsObj => updateLiveActorTimestamp(obj)
  }

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: Exception =>
        println("Vehicle actor exception")
        Escalate
      case t =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  /**
   * Processes an incoming line. If it is for a vehicle already in progress (in Live Actors List), then send a message the respective actor
   * If not already in progress, create a new actor and send the line
   * @param liveSourceLine The incoming source line
   */
  private def processLine(liveSourceLine: TFLSourceLineImpl) = {
    val vehicle_Reg = liveSourceLine.vehicle_Reg
    if (liveActors.contains(vehicle_Reg)) {
      self ! new UpdateLiveActorsObj(vehicle_Reg, liveSourceLine.route_ID, System.currentTimeMillis())
      liveActors(vehicle_Reg)._1 ! liveSourceLine
    } else {
      val newVehicleActor = context.actorOf(Props(new VehicleActor(vehicle_Reg)), vehicle_Reg)
      context.watch(newVehicleActor)
      liveActors += vehicle_Reg -> (newVehicleActor, liveSourceLine.route_ID,System.currentTimeMillis())
      newVehicleActor ! liveSourceLine
    }
    //Update variables
    numberLiveActors = liveActors.size
    numberLiveChildren = context.children.size
  }
  


  /**
   * Update the live actor timestamp whenever there is activity for a vehicle.
   * This indicates the vehicle is still in progress and does not need to be killed
   * @param obj The Update Live Actors Objet
   */
  private def updateLiveActorTimestamp(obj: UpdateLiveActorsObj) = {
    val currentValue = liveActors.get(obj.reg)
    if (currentValue.isDefined) liveActors += obj.reg -> (currentValue.get._1, obj.routeID, obj.timeStamp)
    if (System.currentTimeMillis() - TIME_OF_LAST_CLEANUP > TIME_BETWEEN_CLEANUPS) self ! CleanUp
  }

  /**
   * Periodically clean up the list of live actors to remove those that have not had activity recently (probably withdrawn)
   */
  private def cleanUpLiveActorsList() = {
    TIME_OF_LAST_CLEANUP = System.currentTimeMillis()
    val cutOffThreshold = System.currentTimeMillis() - IDLE_TIME_UNTIL_ACTOR_KILLED
    val actorsToKill = liveActors.filter(x => x._2._3 < cutOffThreshold)
    actorsToKill.foreach(x => {
      killActor(new KillMessage(x._1, x._2._2)) //Kill actor
    })
  }

  /**
   * Kills an actor by sending it a poison pill and sending message to clients to remove
   * @param km The kill message containing vehicle ID and route
   */
  private def killActor(km: KillMessage) = {
    val value = liveActors.get(km.vehicleID)
    if (value.isDefined) value.get._1 ! PoisonPill
    pushToClients(new PackagedStreamObject(km.vehicleID, "kill", Array(), km.routeID, 0, "0", "0", "0")) //Send kill to stream Queue so this is updated for clients
  }

}
