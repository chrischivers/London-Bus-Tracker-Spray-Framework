package com.PredictionAlgorithm.DataSource

import org.apache.http.auth.AuthScope

trait DataSourceVariablesInterface {

  val URL: String
  val USERNAME: String
  val PASSWORD: String
  val CONNECTION_TIMEOUT:Int
  val AUTHSCOPE: AuthScope
  val NUMBER_LINES_TO_DISREGARD: Int

  val FIELD_ORDER: Vector[FieldNames]
}
