package com.PredictionAlgorithm.DataSource.TFL

import com.PredictionAlgorithm.DataSource.DataSourceVariablesInterface
import org.apache.http.auth.AuthScope

/**
 * Created by chrischivers on 18/06/15.
 */
object TFLDataSourceVariables extends DataSourceVariablesInterface {
  val URL = "http://countdown.api.tfl.gov.uk:80/interfaces/ura/stream_V1?ReturnList=StopCode1,LineName,DirectionID,RegistrationNumber,EstimatedTime"
  val USERNAME = "LiveBus78505"
  val PASSWORD = "f4trupHuT3"
  val CONNECTION_TIMEOUT:Int = 3000
  val AUTHSCOPE = new AuthScope("countdown.api.tfl.gov.uk", 80)
  val NUMBER_LINES_TO_DISREGARD = 1

  val FIELD_ORDER = Vector(BUS_STOP_CODE, BUS_ROUTE, BUS_DIRECTION_ID, BUS_REG, BUS_ARRIVAL_TIME)
}
