package com.PredictionAlgorithm.Processes.TFL

import com.PredictionAlgorithm.DataDefinitions.TFL.TFLRouteDefinitions
import com.PredictionAlgorithm.DataSource.SourceLine
import com.PredictionAlgorithm.DataSource.TFL.{TFLSourceLine, TFLDataSource}
import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Database.POINT_TO_POINT_DOCUMENT
import com.PredictionAlgorithm.Database.TFL.{TFLMongoDBConnection, TFLInsertPointToPointDuration}
import com.PredictionAlgorithm.Processes.ProcessLinesInterface

import scala.util.{Failure, Success, Try}


object TFLProcessSourceLines {

  // Map of (Route ID, Vehicle Reg, Direction ID) -> (Stop ID, Arrival Timestamp)
  private var holdingBuffer: Map[(String, String, Int), (String, Long)] = Map()
  val tflRouteDefinitions = TFLRouteDefinitions.getTFLSequenceMap

  def getBufferSize: Int = holdingBuffer.size

  def apply(newLine: TFLSourceLine) {
    if (inDefinitionFile(newLine)) {
      if (!holdingBuffer.contains(newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID)) {

        if (!isFinalStop(newLine)) {
          holdingBuffer += ((newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID) ->(newLine.stop_Code, newLine.arrival_TimeStamp))
        }
      } else {
        val existingValues = holdingBuffer(newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID)
        val existingStopCode = existingValues._1
        val existingArrivalTimeStamp = existingValues._2
        val existingPointSequence = getPointSequence(newLine.route_ID, newLine.direction_ID, existingStopCode)
        val newPointSequence = getPointSequence(newLine.route_ID, newLine.direction_ID, newLine.stop_Code)
        if (newPointSequence == existingPointSequence + 1) {
          val duration = newLine.arrival_TimeStamp - existingArrivalTimeStamp
          if (duration > 0) {
            TFLInsertPointToPointDuration.insertDocument(createPointToPointDocument(newLine.route_ID, newLine.direction_ID, existingStopCode, newLine.stop_Code, "DAY", existingValues._2, duration, System.currentTimeMillis))
          } else {
            // Replace existing values with new values
            holdingBuffer += ((newLine.route_ID, newLine.vehicle_Reg, newLine.direction_ID) ->(newLine.stop_Code, newLine.arrival_TimeStamp))
          }
        }

      }
    } else {
      //TODO log this
      println("Line cannot be found in definition file: " + newLine
      )
    }
  }

  def inDefinitionFile(line: TFLSourceLine): Boolean = {
    if (tflRouteDefinitions.get(line.route_ID, line.direction_ID, line.stop_Code).isEmpty) false else true
  }

  def isFinalStop(line: TFLSourceLine): Boolean = {
    tflRouteDefinitions(line.route_ID, line.direction_ID, line.stop_Code)._2 == Some("Last")
  }

  def getPointSequence(route_ID: String, direction_ID: Int, stop_Code: String): Int = {
    tflRouteDefinitions(route_ID, direction_ID, stop_Code)._1
  }

  def createPointToPointDocument(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Type: String, dep_Time: Long, duration: Long, insert_TimeStamp: Long): POINT_TO_POINT_DOCUMENT = {
    new POINT_TO_POINT_DOCUMENT(route_ID, direction_ID, from_Point_ID, to_Point_ID, day_Type, dep_Time, duration, insert_TimeStamp)
  }

}
