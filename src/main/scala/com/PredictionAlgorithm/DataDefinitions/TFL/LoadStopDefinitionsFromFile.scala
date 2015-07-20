package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io.File

import com.PredictionAlgorithm.DataDefinitions.LoadResource
import com.PredictionAlgorithm.DataDefinitions.TFL.LoadRouteDefinitionsFromFile._

import scala.io.Source

/**
 * Created by chrischivers on 20/07/15.
 */
object LoadStopDefinitionsFromFile extends LoadResource{


  private val stopDefFile = new File(DEFAULT_RESOURCES_LOCATION + DEFAULT_STOP_DEFINITIONS_FILE_NAME)
  // Maps StopCode -> (StopPointName;StopPointType;Towards;Bearing;StopPointIndicator;StopPointState;Latitude;Longitude)
  var stopDefinitionMap:Map[String,(String,String,String,Int,String,Int,Double,Double)] = Map()

  private val s = Source.fromFile(stopDefFile)
  s.getLines.drop(1).foreach((line) => {
    //drop first row and iterate through others
    try {
      val splitLine = line.split(";")
      val stopCode = splitLine(0)
      val stopName = splitLine(1)
      val stopType = splitLine(2)
      val towards = splitLine(3)
      val bearing = splitLine(4).toInt
      val indicator = splitLine(5)
      val state = splitLine(6).toInt
      val lat = splitLine(7).toDouble
      val long = splitLine(8).toDouble

      stopDefinitionMap += (stopCode ->(stopName,stopType,towards,bearing,indicator,state,lat,long))
    }
    catch {
      case e: Exception => throw new Exception("Error reading stop definition file. Error on line: " + line)
    }
  })
  println("Stop Definitions from file loaded")


}
