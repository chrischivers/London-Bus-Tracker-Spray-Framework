package com.PredictionAlgorithm.Processes.TFL

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

  def getBufferSize: Int = holdingBuffer.size

  def apply(line: TFLSourceLine) {
   if (!holdingBuffer.contains(line.route_ID, line.vehicle_Reg, line.direction_ID)) {

     if (true) { // TODO if stop is not at the end of a route
       holdingBuffer += ((line.route_ID, line.vehicle_Reg, line.direction_ID) -> (line.stop_Code, line.arrival_TimeStamp))
       println("holding buffer size: " + getBufferSize)
     }
   } else {
     val values = holdingBuffer(line.route_ID, line.vehicle_Reg, line.direction_ID)
     //TODO more
   }


  }
}
