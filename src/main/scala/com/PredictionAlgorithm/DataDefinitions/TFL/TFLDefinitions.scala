package com.PredictionAlgorithm.DataDefinitions.TFL

import com.PredictionAlgorithm.DataDefinitions.Tools.FetchPolyLines

case class StopDefinitionFields(stopPointName:String, stopPointType:String, towards:String, bearing:Int, stopPointIndicator:String, stopPointState:Int, latitude:String, longitude:String)

object TFLDefinitions {

  //lazy val TFLSequenceMap:Map[(String, Int, String), (Int, Option[String])] = LoadRouteDefinitionsFromWebsite.getMap
  lazy val RouteDefinitionMap:Map[(String, Int), List[(Int, String, Option[String], String)]] =  LoadRouteDefinitions.getRouteDefinitionMap
  lazy val StopDefinitions: Map[String,StopDefinitionFields] = LoadStopDefinitions.getStopDefinitionMap
  lazy val RouteIgnoreList: Set[String] = LoadRouteIgnoreListFromFile.routeIgnoreSet
  lazy val StopIgnoreList: Set[String] = LoadStopIgnoreListFromFile.stopIgnoreSet

  def updateRouteDefinitionsFromWeb() = {
    LoadRouteDefinitions.updateFromWeb()
  }

  def updateStopDefinitionsFromWeb() = {
    LoadStopDefinitions.updateFromWeb()
  }

  def addPolyLinesFromWeb() = {
    FetchPolyLines.updateAll()
  }
}