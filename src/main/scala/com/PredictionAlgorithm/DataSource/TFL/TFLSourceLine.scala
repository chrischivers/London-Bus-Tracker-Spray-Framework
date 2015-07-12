package com.PredictionAlgorithm.DataSource.TFL

import com.PredictionAlgorithm.DataSource._

/**
 * Created by chrischivers on 19/06/15.
 */
class TFLSourceLine(val stop_Code: String, val route_ID: String, val direction_ID: Int, val vehicle_Reg: String, val arrival_TimeStamp: Long) extends SourceLine {

  override def geFieldValueMap(): Map[String, Any] = {
    TFLDataSource.fieldVector
      .zip(Vector(route_ID, direction_ID, vehicle_Reg, stop_Code, arrival_TimeStamp)) //zips array fields with their key values
      .toMap
  }

  override def toString: String = "Line(" + stop_Code + "," + route_ID + "," + direction_ID + "," + vehicle_Reg + "," + arrival_TimeStamp + ")"
}
