package com.PredictionAlgorithm.DataDefinitions

/**
 * Created by chrischivers on 17/07/15.
 */
trait LoadResource {

  val DEFAULT_RESOURCES_LOCATION = "src/main/resources/"
  val DEFAULT_ROUTE_DEFINITIONS_FILE_NAME = "routeSequence.csv"
  val DEFAULT_ROUTE_LIST_FILE_NAME = "routeList.csv"
  val DEFAULT_ROUTE_IGNORE_LIST_FILE_NAME = "routeIgnoreList.csv"
  val DEFAULT_STOP_IGNORE_LIST_FILE_NAME = "stopIgnoreList.csv"
  val DEFAULT_VARIABLES_FILE_NAME = "variables.dat"
  val DEFAULT_STOP_DEFINITIONS_FILE_NAME = "stopdefinitions.csv"

  val TIME_BETWEEN_UPDATES = 604800000

  val LAST_UPDATED_VARIABLE_NAME = "LAST_UPDATED"
}
