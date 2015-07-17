package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io.File

import com.PredictionAlgorithm.DataDefinitions.LoadResource

import scala.io.Source

/**
 * Created by chrischivers on 17/07/15.
 */
object LoadRouteIgnoreListFromFile extends LoadResource{


  private val routeIgnoreListFile = new File(DEFAULT_RESOURCES_LOCATION + DEFAULT_ROUTE_IGNORE_LIST_FILE_NAME)

  lazy val routeIgnoreSet:Set[String] = {
    var routeIgnoreSet:Set[String] = Set()
    val s = Source.fromFile(routeIgnoreListFile)
    s.getLines.drop(1).foreach((line) => {
      //drop first row and iterate through others
      try {
        val splitLine = line.split(",")
        routeIgnoreSet += splitLine(0)
      }
      catch {
        case e: Exception => throw new Exception("Error reading route ignore list file. Error on line: " + line)
      }
    })
    println("Route Ignore List Loaded")
    routeIgnoreSet
  }
}
