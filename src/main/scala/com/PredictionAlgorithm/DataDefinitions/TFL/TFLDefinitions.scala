package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io.File

import com.PredictionAlgorithm.DataDefinitions.TFL
import com.PredictionAlgorithm.DataDefinitions.TFL.LoadRouteIgnoreListFromFile._
import com.PredictionAlgorithm.DataDefinitions.Tools.AddPolyLines

import scala.io.Source

case class StopDefinitionFields(stopPointName:String, stopPointType:String, towards:String, bearing:Int, stopPointIndicator:String, stopPointState:Int, latitude:Double, longitude:Double)

object TFLDefinitions {

  //lazy val TFLSequenceMap:Map[(String, Int, String), (Int, Option[String])] = LoadRouteDefinitionsFromWebsite.getMap
  lazy val StopToPointSequenceMap: Map[(String, Int, String), (Int, Option[String])] =  LoadRouteDefinitions.getStopToPointSequenceMap
  lazy val PointToStopSequenceMap: Map[(String, Int, Int), (String, Option[String])] = LoadRouteDefinitions.getPointToStopSequenceMap
  lazy val RouteDirSequenceList: List[(String, Int, Int, String, Option[String])] = LoadRouteDefinitions.getRouteDirSequenceList
  lazy val StopDefinitions: Map[String,StopDefinitionFields] = LoadStopDefinitions.getStopDefinitionMap
  lazy val RouteIgnoreList: Set[String] = LoadRouteIgnoreListFromFile.routeIgnoreSet
  lazy val StopIgnoreList: Set[String] = LoadStopIgnoreListFromFile.stopIgnoreSet

  def updateRouteDefinitionsFromWeb = {
    LoadRouteDefinitions.updateFromWeb
  }

  def updateStopDefinitionsFromWeb = {
    LoadStopDefinitions.updateFromWeb
  }

  def addPolyLinesFromWeb = {
    AddPolyLines.add
  }
}