package com.predictionalgorithm.datadefinitions.tfl


import com.predictionalgorithm.datadefinitions.LoadResourceFromFile

object LoadRouteIgnoreList extends LoadResourceFromFile{

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
        case e: Exception => throw new Exception("Error reading route ignore list file. Error on line: " + line)
      }
    })
    println("Route Ignore List Loaded")
    routeIgnoreSet
  }
}
