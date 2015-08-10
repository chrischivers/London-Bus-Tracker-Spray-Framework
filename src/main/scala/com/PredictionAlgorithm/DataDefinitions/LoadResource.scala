package com.PredictionAlgorithm.DataDefinitions

import com.PredictionAlgorithm.Database.DatabaseCollections

import scala.io.Source

/**
 * Created by chrischivers on 17/07/15.
 */
trait LoadResource {

  val DEFAULT_ROUTE_LIST_FILE = Source.fromURL(getClass.getResource("/routeList.csv"))
  val DEFAULT_ROUTE_IGNORE_LIST_FILE = Source.fromURL(getClass.getResource("/routeIgnoreList.csv"))
  val DEFAULT_STOP_IGNORE_LIST_FILE = Source.fromURL(getClass.getResource("/stopIgnoreList.csv"))
  //val DEFAULT_VARIABLES_FILE = Source.fromURL(getClass.getResource("/variables.dat"))
  //val DEFAULT_STOP_DEFINITIONS_FILE = Source.fromURL(getClass.getResource("/stopdefinitions.csv"))

  val TIME_BETWEEN_UPDATES = 604800000

  val LAST_UPDATED_VARIABLE_NAME = "LAST_UPDATED"
}
