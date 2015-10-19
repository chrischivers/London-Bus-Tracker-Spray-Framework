package com.predictionalgorithm.datadefinitions.tfl.loadresources

import com.predictionalgorithm.datadefinitions.LoadResourceFromSource
import com.typesafe.scalalogging.LazyLogging

object LoadRouteIgnoreList extends LoadResourceFromSource with LazyLogging {

  override val bufferedSource = DEFAULT_ROUTE_IGNORE_LIST_FILE

  lazy val routeIgnoreSet:Set[String] = {
    var routeIgnoreSet:Set[String] = Set()
    bufferedSource.getLines().drop(1).foreach((line) => {
      //drop first row and iterate through others
      try {
        val splitLine = line.split(",")
        routeIgnoreSet += splitLine(0)
      }
      catch {
        case e: Exception =>
          logger.error("Error reading route ignore list file. Error on line: " + line)
          throw new Exception("Error reading route ignore list file. Error on line: " + line)
      }
    })
    logger.info("Route Ignore List Loaded")
    routeIgnoreSet
  }
}
