package com.predictionalgorithm.datasource.tfl

import com.predictionalgorithm.datasource.DataSource
import org.apache.http.auth.AuthScope


object TFLDataSourceImpl extends DataSource {
  override val URL = "http://countdown.api.tfl.gov.uk:80/interfaces/ura/stream_V1?ReturnList=StopCode1,LineName,DirectionID,RegistrationNumber,EstimatedTime"
  //override val URL = "http://countdown.api.tfl.gov.uk:80/interfaces/ura/stream_V1?LineName=3&ReturnList=StopCode1,LineName,DirectionID,RegistrationNumber,EstimatedTime"
  override val USERNAME = "LiveBus78505"
  override val PASSWORD = "f4trupHuT3"
  override  val CONNECTION_TIMEOUT:Int = 3000
  override val AUTHSCOPE = new AuthScope("countdown.api.tfl.gov.uk", 80)
  override val NUMBER_LINES_TO_DISREGARD = 1

}
