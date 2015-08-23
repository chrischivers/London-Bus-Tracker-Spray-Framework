package com.PredictionAlgorithm.DataDefinitions

import scala.io.Source


trait LoadResource {

  val DEFAULT_ROUTE_LIST_FILE = Source.fromURL(getClass.getResource("/routeList.csv"))
  val DEFAULT_ROUTE_IGNORE_LIST_FILE = Source.fromURL(getClass.getResource("/routeIgnoreList.csv"))
  val DEFAULT_STOP_IGNORE_LIST_FILE = Source.fromURL(getClass.getResource("/stopIgnoreList.csv"))


  val TIME_BETWEEN_UPDATES = 604800000

  val LAST_UPDATED_VARIABLE_NAME = "LAST_UPDATED"
}
