package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io.File

import com.PredictionAlgorithm.DataDefinitions.TFL
import com.PredictionAlgorithm.DataDefinitions.TFL.LoadRouteIgnoreListFromFile._

import scala.io.Source


object TFLRouteDefinitions {

lazy val loadFromWeb :Boolean = if((System.currentTimeMillis() - getLastUpdatedVariable.getOrElse(0.toLong)) > TIME_BETWEEN_UPDATES) true else false

  //lazy val TFLSequenceMap:Map[(String, Int, String), (Int, Option[String])] = LoadRouteDefinitionsFromWebsite.getMap
  lazy val StopToPointSequenceMap:Map[(String, Int, String), (Int, Option[String])] = if (loadFromWeb) LoadRouteDefinitionsFromWebsite.getStopToPointSequenceMap else LoadRouteDefinitionsFromFile.getStopToPointSequenceMap
  lazy val PointToStopSequenceMap:Map[(String, Int, Int), (String, Option[String])] = if (loadFromWeb) LoadRouteDefinitionsFromWebsite.getPointToStopSequenceMap else LoadRouteDefinitionsFromFile.getPointToStopSequenceMap
  lazy val RouteIgnoreList:Set[String] = LoadRouteIgnoreListFromFile.routeIgnoreSet
  lazy val StopIgnoreList:Set[String] = LoadStopIgnoreListFromFile.stopIgnoreSet


  def getLastUpdatedVariable : Option[Long] = {
    val variablesFile = new File(DEFAULT_RESOURCES_LOCATION + DEFAULT_VARIABLES_FILE_NAME)
    val s = Source.fromFile(variablesFile)
    for (line <- s.getLines()) {
      println(line)
         if (line.startsWith(LAST_UPDATED_VARIABLE_NAME)) {
            return Option(line.splitAt(LAST_UPDATED_VARIABLE_NAME.length + 1)._2.toLong)
         }
    }
    None
  }

}