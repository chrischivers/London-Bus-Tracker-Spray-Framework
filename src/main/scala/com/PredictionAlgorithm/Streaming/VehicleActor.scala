package com.PredictionAlgorithm.Streaming

import akka.actor.Actor
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.DataDefinitions.TFL.{StopDefinitionFields, TFLDefinitions}
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Prediction.{KNNPrediction, PredictionRequest}

import scala.concurrent.duration._

/**
 * Created by chrischivers on 04/08/15.
 */
class VehicleActor(vehicle_ID: String) extends Actor {

  import context.dispatcher
  val DEFAULT_DURATION_WHERE_PREDICTION_NOT_AVAILABLE:Double = 45

  var StopList: List[String] = List()
  var receivedFirstLine = false
  var lastIndexSentForProcessing = -1
  var nextStopArrivalDueAt: Long = -1
  var currentRouteID:String = _
  var currentDirectionID:Int = _

  override def receive: Actor.Receive = {
    case sourceLine: TFLSourceLine => if(receivedLineValid(sourceLine)) process(sourceLine.route_ID,sourceLine.direction_ID,sourceLine.arrival_TimeStamp,sourceLine.stop_Code)
    case indexOfNextStopToCalculate:Int => handleNextStopCalculation(indexOfNextStopToCalculate)
  }

  def buildStopList (routeID: String, directionID: Int) = {
    StopList = TFLDefinitions.RouteDefinitionMap(routeID,directionID).sortBy(_._1).map{ case (sequence,stop,firstLast,polyline) => stop}
   // println("Veh: " + vehicle_ID + "StopList: " + StopList)
  }

  def receivedLineValid(sourceLine: TFLSourceLine): Boolean = {

    if (receivedFirstLine) {
      val indexOfStopCode = StopList.indexOf(sourceLine.stop_Code)
      if (sourceLine.route_ID == currentRouteID && sourceLine.direction_ID== currentDirectionID) {
        if (indexOfStopCode == lastIndexSentForProcessing + 1 && indexOfStopCode != StopList.length - 1) true
        else false
      } else {
        receivedFirstLine = false
        receivedLineValid(sourceLine)
      }
    } else {
      receivedFirstLine = true
      currentRouteID = sourceLine.route_ID
      currentDirectionID = sourceLine.direction_ID
      buildStopList(currentRouteID, currentDirectionID)
      val indexOfStopCode = StopList.indexOf(sourceLine.stop_Code)
      if (indexOfStopCode != StopList.length - 1) true
      else false
    }
  }


  def handleNextStopCalculation(nextStopIndex:Int) = {
    if (nextStopIndex == lastIndexSentForProcessing + 1 && nextStopIndex != StopList.length - 1) {
      val routeID = currentRouteID
      val directionID = currentDirectionID
      val arrivalTime = nextStopArrivalDueAt
      val stopCode = StopList(nextStopIndex)
      process(routeID,directionID,arrivalTime,stopCode)
    }
  }

  def process(routeID:String, directionID:Int, arrivalTime:Long, stopCode:String) = {

    val indexOfStopCode = StopList.indexOf(stopCode)
    lastIndexSentForProcessing = indexOfStopCode
   // println("veh: " + vehicle_ID + ". lastindexSentforProcessing: " + lastIndexSentForProcessing + ". StopCode: " + stopCode + ". stopList length - 1: " + (StopList.length - 1) + "RouteID: " + routeID + ". Direction ID:" + directionID)
    val polyLineToNextStop = TFLDefinitions.RouteDefinitionMap(routeID,directionID)(indexOfStopCode)._4
    val decodedPolyLineToNextStop = Commons.decodePolyLine(polyLineToNextStop)

    val nextStopCode = StopList(indexOfStopCode + 1)
    val indexOfNextStopCode = indexOfStopCode + 1

    val predictionRequest = new PredictionRequest(routeID, directionID, stopCode, nextStopCode, Commons.getDayCode(arrivalTime), Commons.getTimeOffset(arrivalTime))
    val predictedDurtoNextStop_MS = KNNPrediction.makePredictionBetweenConsecutivePoints(predictionRequest).getOrElse(DEFAULT_DURATION_WHERE_PREDICTION_NOT_AVAILABLE) * 1000

    //Holds back until previous has finished (prevents interuptions)
    val transmitTime = if (arrivalTime < nextStopArrivalDueAt) nextStopArrivalDueAt else arrivalTime
    //println("Veh: " + vehicle_ID + ". transmitTime - System.currentTimeMillis(): " + (transmitTime - System.currentTimeMillis()))

    in(Duration(transmitTime - System.currentTimeMillis() + 2000, MILLISECONDS)) {

      nextStopArrivalDueAt = arrivalTime + predictedDurtoNextStop_MS.toLong

      // println("Veh: " + vehicle_ID + ". Relative duration: " + relativeDuration)
      val pso = new PackagedStreamObject(vehicle_ID,nextStopArrivalDueAt.toString,decodedPolyLineToNextStop,routeID,directionID,"TODO",stopCode, TFLDefinitions.StopDefinitions(stopCode).stopPointName)
      LiveStreamingCoordinator.enqueue(pso)
      LiveStreamingCoordinator.updateLiveActorTimestamp(vehicle_ID)

      val relativeDuration = nextStopArrivalDueAt - System.currentTimeMillis()
      in(Duration(relativeDuration, MILLISECONDS)) {
        self ! indexOfNextStopCode
      }
    }


  }
  def in[U](duration: FiniteDuration)(body: => U): Unit =
    LiveStreamingCoordinator.actorSystem.scheduler.scheduleOnce(duration)(body)
}
