package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io.File

import com.PredictionAlgorithm.DataDefinitions.LoadResource

import scala.io.Source


object LoadRouteIgnoreListFromFile extends LoadResource{


  private val routeIgnoreListFile = DEFAULT_ROUTE_IGNORE_LIST_FILE

  lazy val routeIgnoreSet:Set[String] = {
    var routeIgnoreSet:Set[String] = Set()
    routeIgnoreListFile.getLines.drop(1).foreach((line) => {
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
