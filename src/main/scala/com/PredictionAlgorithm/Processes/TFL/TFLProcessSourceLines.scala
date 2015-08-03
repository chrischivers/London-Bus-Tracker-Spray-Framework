package com.PredictionAlgorithm.Processes.TFL

import java.util.{Calendar, Date, GregorianCalendar}

import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.DataSource.TFL.{TFLSourceLine, TFLDataSource}
import com.PredictionAlgorithm.Database.POINT_TO_POINT_DOCUMENT
import com.PredictionAlgorithm.Database.TFL.{TFLMongoDBConnection, TFLInsertPointToPointDuration}
import com.PredictionAlgorithm.Streaming.LiveStreamingCoordinator
import grizzled.slf4j.Logger

import scala.util.{Failure, Success, Try}

class TFLProcessSourceLines

object TFLProcessSourceLines {

  val logger = Logger(classOf[TFLProcessSourceLines])
  val MAXIMUM_AGE_OF_RECORDS_IN_HOLDING_BUFFER = 600000 //In Ms
  var numberNonMatches = 0

  // Map of (Route ID, Vehicle Reg, Direction ID) -> (Stop ID, Arrival Timestamp)
  private var holdingBuffer: Map[(String, String, Int), (String, Long)] = Map()
  private var liveStreamCollectionEnabled:Boolean = false
  private var historicalDataStoringEnabled = false
  val stopIgnoreList = TFLDefinitions.StopIgnoreList
  val routeIgnoreList = TFLDefinitions.RouteIgnoreList

  def getBufferSize: Int = holdingBuffer.size

  def apply(newLine: TFLSourceLine) {
    if (validateLine(newLine)) {
      // Send to Live Streaming Coordinator if Enabled
      if (liveStreamCollectionEnabled) LiveStreamingCoordinator.setObjectPosition(newLine)
      if (historicalDataStoringEnabled) {
        if (!isFinalStop(newLine)) {
          if (!holdingBuffer.contains(newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID)) {
            holdingBufferAddAndPrune(newLine)
          } else {
            val existingValues = holdingBuffer(newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID)
            val existingStopCode = existingValues._1
            val existingArrivalTimeStamp = existingValues._2
            val existingPointSequence = TFLDefinitions.RouteDefinitionMap(newLine.route_ID, newLine.direction_ID).filter(x=> x._2 == existingStopCode).head._1
            val newPointSequence = TFLDefinitions.RouteDefinitionMap(newLine.route_ID, newLine.direction_ID).filter(x=> x._2 == newLine.stop_Code).last._1
            if (newPointSequence == existingPointSequence + 1) {
              val durationInSeconds = ((newLine.arrival_TimeStamp - existingArrivalTimeStamp) / 1000).toInt
              if (durationInSeconds > 0) {
                TFLInsertPointToPointDuration.insertDocument(createPointToPointDocument(newLine.route_ID, newLine.direction_ID, existingStopCode, newLine.stop_Code, Commons.getDayCode(existingArrivalTimeStamp), Commons.getTimeOffset(existingArrivalTimeStamp), durationInSeconds))
                holdingBufferAddAndPrune(newLine)
              } else {
                holdingBufferAddAndPrune(newLine) // Replace existing values with new values
              }
            } else if (newPointSequence >= existingPointSequence) {
              holdingBufferAddAndPrune(newLine) // Replace existing values with new values
            } else {
              // DO Nothing
            }
          }
        }
      }
    }
  }

  def holdingBufferAddAndPrune(line:TFLSourceLine) = {
    holdingBuffer += ((line.route_ID, line.vehicle_Reg, line.direction_ID) ->(line.stop_Code, line.arrival_TimeStamp))
    val CUT_OFF:Long = System.currentTimeMillis() - MAXIMUM_AGE_OF_RECORDS_IN_HOLDING_BUFFER
    holdingBuffer = holdingBuffer.filter{case ((_),(_,time)) => time > CUT_OFF}
  }

  def validateLine(line: TFLSourceLine): Boolean = {

    if (line.route_ID == "288" && line.direction_ID == 1 && line.stop_Code == "76069") {
      println("Result: " + inDefinitionFile(line))
    }

    def inDefinitionFile(line: TFLSourceLine): Boolean = {
      if (TFLDefinitions.RouteDefinitionMap.get(line.route_ID, line.direction_ID).isEmpty) {
        numberNonMatches += 1
        //logger.info("Cannot get definition. Line: " + line) //TODO Fix this
         false
      } else if (!TFLDefinitions.RouteDefinitionMap(line.route_ID, line.direction_ID).exists(x => x._2 == line.stop_Code)) {
        numberNonMatches += 1
         false
      }
      else  true
    }

    def isWithinTimeThreshold(line: TFLSourceLine): Boolean = {
      ((line.arrival_TimeStamp - System.currentTimeMillis)/1000) <= TFLProcessVariables.LINE_TOLERANCE_IN_RELATION_TO_CURRENT_TIME
    }

    def isNotOnIgnoreLists(line: TFLSourceLine): Boolean = {
      if (routeIgnoreList.contains(line.route_ID) || stopIgnoreList.contains(line.stop_Code)) false else true
    }

    if (!isNotOnIgnoreLists(line)) return false
    if (!inDefinitionFile(line)) return false
    if (!isWithinTimeThreshold(line)) return false
    true
  }

  def isFinalStop(line: TFLSourceLine): Boolean = TFLDefinitions.RouteDefinitionMap(line.route_ID, line.direction_ID).filter(x => x._2 == line.stop_Code).head._3.contains("LAST")


  def createPointToPointDocument(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Type: String, observed_Time: Int, durationSeconds: Int): POINT_TO_POINT_DOCUMENT = {
    new POINT_TO_POINT_DOCUMENT(route_ID, direction_ID, from_Point_ID, to_Point_ID, day_Type, observed_Time, durationSeconds)
  }

  def setLiveStreamCollection(enabled: Boolean) = {
    liveStreamCollectionEnabled = enabled
  }

  def setHistoricalDataStoring(enabled: Boolean) = {
    historicalDataStoringEnabled = enabled
  }


}
