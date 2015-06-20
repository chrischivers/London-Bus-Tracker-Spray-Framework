package com.PredictionAlgorithm.TFL.DataSource

import org.apache.http.auth.AuthScope

/**
 * Created by chrischivers on 18/06/15.
 */
object DataSourceVariables {
  val URL = "http://countdown.api.tfl.gov.uk:80/interfaces/ura/stream_V1?ReturnList=StopCode1,LineName,DirectionID,RegistrationNumber,EstimatedTime"
  val USERNAME = "LiveBus78505"
  val PASSWORD = "f4trupHuT3"
  val CONNECTION_TIMEOUT:Int = 3000
  lazy val AUTHSCOPE = new AuthScope("countdown.api.tfl.gov.uk", 80)
  val NUMBER_LINES_TO_DISREGARD = 1
}
