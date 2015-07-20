package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io.File

import com.PredictionAlgorithm.DataDefinitions.LoadResource

import scala.collection.immutable.ListMap
import scala.io.Source

/**
 * Created by chrischivers on 12/07/15.
 */
object LoadRouteDefinitionsFromFile extends LoadResource{


    private val routeDefFile = new File(DEFAULT_RESOURCES_LOCATION + DEFAULT_ROUTE_DEFINITIONS_FILE_NAME)
    var StopToPointSequenceMap: Map[(String, Int, String), (Int, Option[String])] = ListMap() //Empty map
    var PointToStopSequenceMap: Map[(String, Int, Int), (String, Option[String])] = ListMap()

    private val s = Source.fromFile(routeDefFile)
    s.getLines.drop(1).foreach((line) => {
      //drop first row and iterate through others
      try {
        val splitLine = line.split(";")
        val route_ID = splitLine(0)
        val direction_ID = splitLine(1).toInt
        val pointsSequence = splitLine(2).toInt
        val busStopID = splitLine(3)
        val first_last: Option[String] = {
          if (splitLine.length == 5) Option(splitLine(4))
          else None
        }
        StopToPointSequenceMap += ((route_ID, direction_ID, busStopID) ->(pointsSequence, first_last))
        PointToStopSequenceMap += ((route_ID, direction_ID, pointsSequence) ->(busStopID, first_last))
      }
      catch {
        case e: Exception => throw new Exception("Error reading route definition file. Error on line: " + line)
      }
    })
  println("Route Definitions from file loaded")


}
