package com.PredictionAlgorithm.Streaming

import akka.actor.{PoisonPill, Actor}
import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.DataDefinitions.TFL.{StopDefinitionFields, TFLDefinitions}
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine
import com.PredictionAlgorithm.Prediction.{KNNPrediction, PredictionRequest}

import scala.concurrent.duration._

class VehicleActor(vehicle_ID: String) extends Actor {

  import context.dispatcher
  val DEFAULT_DURATION_WHERE_PREDICTION_NOT_AVAILABLE:Double = 45
  val SPEED_UP_MODE_TIME_MULTIPLIER = 0.5

  var StopList: List[String] = List()
  var receivedFirstLine = false
  var pauseAutoProcessing:Boolean = false
  var speedUpNumber = 0
  var lastIndexSentForProcessing = -1
  var nextStopArrivalDueAt: Long = -1
  var currentRouteID:String = _
  var currentDirectionID:Int = _

  override def receive: Actor.Receive = {
    case sourceLine: TFLSourceLine => if(receivedLineValid(sourceLine)) {
      process(sourceLine.route_ID,sourceLine.direction_ID,sourceLine.arrival_TimeStamp,sourceLine.stop_Code)
      pauseAutoProcessing = false
    }
    case indexOfNextStopToCalculate:Int => if (!pauseAutoProcessing) handleNextStopCalculation(indexOfNextStopToCalculate)
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

        else if (indexOfStopCode <= lastIndexSentForProcessing && indexOfStopCode != StopList.length - 1) {
          // Auto processing ahead of live stream, needs to pause to allow catch up
          if (sourceLine.route_ID == "3") println("Running ahead")
          pauseAutoProcessing = true
          false

        } else if (indexOfStopCode > lastIndexSentForProcessing + 1 && indexOfStopCode != StopList.length - 1) {
          // Auto processing behind, needs to speed up to allow catch up
          val stopsDifference = indexOfStopCode - (lastIndexSentForProcessing + 1)
          for (i <- 1 to stopsDifference) {
            speedUpNumber = speedUpNumber + 1
            self ! lastIndexSentForProcessing + i
          }
          if (sourceLine.route_ID == "3")  println("Running Behind. SpeedUpNumber = " + speedUpNumber)
          true
        } else if (indexOfStopCode == StopList.length - 1) {
          endOfRouteKill//Handle last stop
          false
        } else false

      }else {
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
      val arrivalTime =nextStopArrivalDueAt
      val stopCode = StopList(nextStopIndex)
      process(routeID,directionID,arrivalTime,stopCode)
    } else if (nextStopIndex == StopList.length - 1) endOfRouteKill

  }

  def endOfRouteKill = {
    LiveStreamingCoordinator.killActor(new KillMessage(vehicle_ID,currentRouteID))
  }

  def process(routeID:String, directionID:Int, arrivalTime:Long, stopCode:String) = {

    val indexOfStopCode = StopList.indexOf(stopCode)
    lastIndexSentForProcessing = indexOfStopCode
   // println("veh: " + vehicle_ID + ". lastindexSentforProcessing: " + lastIndexSentForProcessing + ". StopCode: " + stopCode + ". stopList length - 1: " + (StopList.length - 1) + "RouteID: " + routeID + ". Direction ID:" + directionID)
    val polyLineToNextStop = TFLDefinitions.RouteDefinitionMap(routeID,directionID)(indexOfStopCode)._4

    val nextStopCode = StopList(indexOfStopCode + 1)
    val indexOfNextStopCode = indexOfStopCode + 1

    val predictionRequest = new PredictionRequest(routeID, directionID, stopCode, nextStopCode, Commons.getDayCode(arrivalTime), Commons.getTimeOffset(arrivalTime))
    val predictedDurtoNextStop_MS = KNNPrediction.makePredictionBetweenConsecutivePoints(predictionRequest).getOrElse(DEFAULT_DURATION_WHERE_PREDICTION_NOT_AVAILABLE, 1)._1 * 1000

    //Holds back until previous has finished (prevents interuptions)
    val transmitTime = if (arrivalTime < nextStopArrivalDueAt) nextStopArrivalDueAt else arrivalTime
    //println("Veh: " + vehicle_ID + ". transmitTime - System.currentTimeMillis(): " + (transmitTime - System.currentTimeMillis()))

    in(Duration(transmitTime - System.currentTimeMillis() + 2000, MILLISECONDS)) {

      val addedTime = if (speedUpNumber > 0) (predictedDurtoNextStop_MS.toLong * SPEED_UP_MODE_TIME_MULTIPLIER).toLong else predictedDurtoNextStop_MS.toLong
      nextStopArrivalDueAt = arrivalTime + addedTime
      if (speedUpNumber > 0) speedUpNumber  = speedUpNumber - 1

      // println("Veh: " + vehicle_ID + ". Relative duration: " + relativeDuration)
      val movementDataArray = Commons.getMovementDataArray(polyLineToNextStop,routeID)
      val pso = new PackagedStreamObject(vehicle_ID,nextStopArrivalDueAt.toString,movementDataArray,routeID,directionID,"TODO",nextStopCode, TFLDefinitions.StopDefinitions(nextStopCode).stopPointName)
      LiveStreamingCoordinator.enqueue(pso)

      val relativeDuration = nextStopArrivalDueAt - System.currentTimeMillis()
      try {
        in(Duration(relativeDuration, MILLISECONDS)) {
          self ! indexOfNextStopCode
        }
      } catch {
        case e:NullPointerException => //Actor already killed. Do nothing
      }
    }


  }
  def in[U](duration: FiniteDuration)(body: => U): Unit =
    LiveStreamingCoordinator.vehicleSystem.scheduler.scheduleOnce(duration)(body)

}
