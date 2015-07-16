package com.PredictionAlgorithm.Processes.TFL

import java.util.{Calendar, Date, GregorianCalendar}

import com.PredictionAlgorithm.DataDefinitions.TFL.TFLRouteDefinitions
import com.PredictionAlgorithm.DataSource.TFL.{TFLSourceLine, TFLDataSource}
import com.PredictionAlgorithm.Database.POINT_TO_POINT_DOCUMENT
import com.PredictionAlgorithm.Database.TFL.{TFLMongoDBConnection, TFLInsertPointToPointDuration}
import grizzled.slf4j.Logger
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

class TFLProcessSourceLines

object TFLProcessSourceLines {

  val logger = Logger(classOf[TFLProcessSourceLines])
  val MAXIMUM_AGE_OF_RECORDS_IN_HOLDING_BUFFER = 600000

  // Map of (Route ID, Vehicle Reg, Direction ID) -> (Stop ID, Arrival Timestamp)
  private var holdingBuffer: Map[(String, String, Int), (String, Long)] = Map()
  val tflRouteDefinitions = TFLRouteDefinitions.getTFLSequenceMap

  def getBufferSize: Int = holdingBuffer.size

  def apply(newLine: TFLSourceLine) {
    if (validateLine(newLine)) {
      if (!holdingBuffer.contains(newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID)) {
        holdingBuffer += ((newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID) ->(newLine.stop_Code, newLine.arrival_TimeStamp))
      } else {
        val existingValues = holdingBuffer(newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID)
        val existingStopCode = existingValues._1
        val existingArrivalTimeStamp = existingValues._2
        val existingPointSequence = getPointSequence(newLine.route_ID, newLine.direction_ID, existingStopCode)
        val newPointSequence = getPointSequence(newLine.route_ID, newLine.direction_ID, newLine.stop_Code)
        if (newPointSequence == existingPointSequence + 1) {
          val duration = newLine.arrival_TimeStamp - existingArrivalTimeStamp
          if (duration > 0) {
            TFLInsertPointToPointDuration.insertDocument(createPointToPointDocument(newLine.route_ID, newLine.direction_ID, existingStopCode, newLine.stop_Code, getDayCode(existingArrivalTimeStamp), existingArrivalTimeStamp, duration))
            holdingBuffer += ((newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID) ->(newLine.stop_Code, newLine.arrival_TimeStamp))
          } else {
            // Replace existing values with new values
            holdingBuffer += ((newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID) ->(newLine.stop_Code, newLine.arrival_TimeStamp))
          }
        }

      }
    }
    holdingBufferPrune
  }

  def holdingBufferPrune = {
    val CUT_OFF:Long = System.currentTimeMillis() - MAXIMUM_AGE_OF_RECORDS_IN_HOLDING_BUFFER
    holdingBuffer = holdingBuffer.filter{case ((_),(_,time)) => time > CUT_OFF}
  }

  def validateLine(line: TFLSourceLine): Boolean = {

    def inDefinitionFile(line: TFLSourceLine): Boolean = {
      if (tflRouteDefinitions.get(line.route_ID, line.direction_ID, line.stop_Code).isEmpty) {
        logger.info("Cannot get definition. Line: " + line)
        false
      } else true
    }

    def isNotFinalStop(line: TFLSourceLine): Boolean = {
      tflRouteDefinitions(line.route_ID, line.direction_ID, line.stop_Code)._2 != Some("LAST")
    }

    def isWithinTimeThreshold(line: TFLSourceLine): Boolean = {
      (line.arrival_TimeStamp - System.currentTimeMillis) <= TFLProcessVariables.LINE_TOLERANCE_IN_RELATION_TO_CURRENT_TIME
    }


    if (!inDefinitionFile(line)) return false
    if (!isWithinTimeThreshold(line)) return false
    if (!isNotFinalStop(line)) return false
    true
  }

  def getPointSequence(route_ID: String, direction_ID: Int, stop_Code: String): Int = {
    tflRouteDefinitions(route_ID, direction_ID, stop_Code)._1
  }

  def createPointToPointDocument(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Type: String, observed_Time: Long, duration: Long): POINT_TO_POINT_DOCUMENT = {
    new POINT_TO_POINT_DOCUMENT(route_ID, direction_ID, from_Point_ID, to_Point_ID, day_Type, observed_Time, duration)
  }

  def getDayCode(arrivalTime: Long): String = {
    val cal: Calendar  = new GregorianCalendar();

    cal.setTime(new Date(arrivalTime));
    cal.get(Calendar.DAY_OF_WEEK) match {
      case Calendar.SATURDAY => "SAT"
      case Calendar.SUNDAY => "SUN"
      case _ => "MON_FRI"
    }
  }

}
