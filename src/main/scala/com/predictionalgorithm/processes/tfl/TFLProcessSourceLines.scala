package com.predictionalgorithm.processes.tfl

import java.util.Date

import com.predictionalgorithm.commons.Commons
import com.predictionalgorithm.datadefinitions.tfl.TFLDefinitions
import com.predictionalgorithm.datasource.tfl.TFLSourceLine
import com.predictionalgorithm.database.POINT_TO_POINT_DOCUMENT
import com.predictionalgorithm.database.tfl.TFLInsertPointToPointDuration
import com.predictionalgorithm.processes.weather.Weather
import com.predictionalgorithm.streaming.LiveStreamingCoordinator
//import grizzled.slf4j.Logger
import org.apache.commons.lang3.time.DateUtils


class TFLProcessSourceLines

object TFLProcessSourceLines {

  //val logger = Logger(classOf[TFLProcessSourceLines])
  val MAXIMUM_AGE_OF_RECORDS_IN_HOLDING_BUFFER = 600000
  //In Ms
  var numberNonMatches = 0

  // Map of (Route ID, Vehicle Reg, Direction ID) -> (Stop ID, Arrival Timestamp)
  private var holdingBuffer: Map[(String, String, Int), (String, Long)] = Map()
  private var liveStreamCollectionEnabled: Boolean = false
  private var historicalDataStoringEnabled = false

  private val stopIgnoreList = TFLDefinitions.StopIgnoreList
  private val routeIgnoreList = TFLDefinitions.RouteIgnoreList
  private val publicHolidayList = TFLDefinitions.PublicHolidayList

  def getBufferSize: Int = holdingBuffer.size

  def apply(newLine: TFLSourceLine) {

    if (validateLine(newLine)) {
      // Send to Live Streaming Coordinator if Enabled
      if (liveStreamCollectionEnabled) LiveStreamingCoordinator.processSourceLine(newLine)
      if (historicalDataStoringEnabled && !isPublicHoliday(newLine)) {

        if (!holdingBuffer.contains(newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID)) {
          updateHoldingBufferAndPrune(newLine)
        } else {
          val existingValues = holdingBuffer(newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID)
          val existingStopCode = existingValues._1
          val existingArrivalTimeStamp = existingValues._2
          val existingPointSequence = TFLDefinitions.RouteDefinitionMap(newLine.route_ID, newLine.direction_ID).filter(x => x._2 == existingStopCode).head._1
          val newPointSequence = TFLDefinitions.RouteDefinitionMap(newLine.route_ID, newLine.direction_ID).filter(x => x._2 == newLine.stop_Code).last._1
          if (newPointSequence == existingPointSequence + 1) {
            val durationInSeconds = ((newLine.arrival_TimeStamp - existingArrivalTimeStamp) / 1000).toInt
            if (durationInSeconds > 0) {
              TFLInsertPointToPointDuration.insertDocument(createPointToPointDocument(newLine.route_ID, newLine.direction_ID, existingStopCode, newLine.stop_Code, Commons.getDayCode(existingArrivalTimeStamp), Commons.getTimeOffset(existingArrivalTimeStamp), durationInSeconds))
              updateHoldingBufferAndPrune(newLine)
            } else {
              updateHoldingBufferAndPrune(newLine) // Replace existing values with new values
            }
          } else if (newPointSequence >= existingPointSequence) {
            updateHoldingBufferAndPrune(newLine) // Replace existing values with new values
          } else {
            // DO Nothing
          }

        }
      }
    }
  }

  private def updateHoldingBufferAndPrune(line: TFLSourceLine) = {
    if (!isFinalStop(line)) {
      holdingBuffer += ((line.route_ID, line.vehicle_Reg, line.direction_ID) ->(line.stop_Code, line.arrival_TimeStamp))
      val CUT_OFF: Long = System.currentTimeMillis() - MAXIMUM_AGE_OF_RECORDS_IN_HOLDING_BUFFER
      holdingBuffer = holdingBuffer.filter { case ((_), (_, time)) => time > CUT_OFF }
    } else {
      holdingBuffer -= ((line.route_ID, line.vehicle_Reg, line.direction_ID))
    }
  }

  private def validateLine(line: TFLSourceLine): Boolean = {


    def inDefinitionFile(line: TFLSourceLine): Boolean = {
      if (TFLDefinitions.RouteDefinitionMap.get(line.route_ID, line.direction_ID).isEmpty) {
        numberNonMatches += 1
        //logger.info("Cannot get definition. Line: " + line) //TODO Fix this
        false
      } else if (!TFLDefinitions.RouteDefinitionMap(line.route_ID, line.direction_ID).exists(x => x._2 == line.stop_Code)) {
        numberNonMatches += 1
        // println(line.route_ID + "," + line.direction_ID + ":    " + TFLDefinitions.RouteDefinitionMap(line.route_ID, line.direction_ID))
        // println("Non Match: " + line.route_ID + ", " + line.direction_ID + ", " + line.stop_Code)
        false
      }
      else true
    }

    def isWithinTimeThreshold(line: TFLSourceLine): Boolean = {
      ((line.arrival_TimeStamp - System.currentTimeMillis) / 1000) <= TFLProcessVariables.LINE_TOLERANCE_IN_RELATION_TO_CURRENT_TIME
    }

    def isNotOnIgnoreLists(line: TFLSourceLine): Boolean = {
      if (routeIgnoreList.contains(line.route_ID) || stopIgnoreList.contains(line.stop_Code)) false else true
    }

    if (!isNotOnIgnoreLists(line)) return false
    if (!inDefinitionFile(line)) return false
    if (!isWithinTimeThreshold(line)) return false
    true
  }

  private def isFinalStop(line: TFLSourceLine): Boolean = TFLDefinitions.RouteDefinitionMap(line.route_ID, line.direction_ID).filter(x => x._2 == line.stop_Code).head._3.contains("LAST")

  private def isPublicHoliday(line: TFLSourceLine): Boolean = {

    val date = new Date(line.arrival_TimeStamp)
    for (pubHolDate <- publicHolidayList) if (DateUtils.isSameDay(date, pubHolDate)) return true
    false
  }


  private def createPointToPointDocument(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Type: String, observed_Time: Int, durationSeconds: Int): POINT_TO_POINT_DOCUMENT = {
    new POINT_TO_POINT_DOCUMENT(route_ID, direction_ID, from_Point_ID, to_Point_ID, day_Type, observed_Time, durationSeconds, Weather.getCurrentRainfall)
  }

  def setLiveStreamCollection(enabled: Boolean) = {
    liveStreamCollectionEnabled = enabled
  }

  def setHistoricalDataStoring(enabled: Boolean) = {
    historicalDataStoringEnabled = enabled
  }


}
