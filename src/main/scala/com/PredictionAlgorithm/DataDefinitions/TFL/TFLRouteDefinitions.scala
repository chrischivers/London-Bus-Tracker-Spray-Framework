package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io.{FileReader, BufferedReader, File}
import java.lang.ArrayIndexOutOfBoundsException

import scala.io.Source
import scala.util.Try

object  TFLRouteDefinitions {

  // Map format = Route_ID, Direction_ID, BusStopCode, First_Last -> pointsSequence
  private var TFLsequenceMap: Map[(String, Int, String), (Int, Option[String])] = Map()
  private var definitionsLoaded: Boolean = false

  //TODO get these through dependency injection
  val DEFAULT_RESOURCES_LOCATION = "src/main/resources/"
  val DEFAULT_ROUTE_DEFINITIONS_FILE_NAME = "routesequence.csv"

  def getTFLSequenceMap = {
    if (!definitionsLoaded) {
      loadDefinitionsFromFile()
    }
    TFLsequenceMap
  }


  private def loadDefinitionsFromFile(): Unit = {

    val routeDefFile = new File(DEFAULT_RESOURCES_LOCATION + DEFAULT_ROUTE_DEFINITIONS_FILE_NAME)

    val s = Source.fromFile(routeDefFile)
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
        TFLsequenceMap += ((route_ID, direction_ID, busStopID) -> (pointsSequence, first_last))
      }
      catch {
        case e: Exception => throw new Exception("Error reading route definition file. Error on line: " + line)
      }
    })
    definitionsLoaded = true
  }

}