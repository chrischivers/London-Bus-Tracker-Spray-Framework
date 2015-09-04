package com.predictionalgorithm.datadefinitions.tfl

import java.util.Date
import com.predictionalgorithm.datadefinitions.tools.FetchPolyLines

case class StopDefinitionFields(stopPointName:String, stopPointType:String, towards:String, bearing:Int, stopPointIndicator:String, stopPointState:Int, latitude:String, longitude:String)

/**
 * The reference for definitions files
 */
object TFLDefinitions {

  lazy val RouteDefinitionMap:Map[(String, Int), List[(Int, String, Option[String], String)]] =  LoadRouteDefinitions.getRouteDefinitionMap
  lazy val StopDefinitions: Map[String,StopDefinitionFields] = LoadStopDefinitions.getStopDefinitionMap
  lazy val RouteIgnoreList: Set[String] = LoadRouteIgnoreList.routeIgnoreSet
  lazy val StopIgnoreList: Set[String] = LoadStopIgnoreList.stopIgnoreSet
  lazy val PublicHolidayList:List[Date] = LoadPublicHolidayList.publicHolidayList

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