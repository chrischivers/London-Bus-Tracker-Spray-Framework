package com.predictionalgorithm.datadefinitions.tfl

import java.util.Date
import com.predictionalgorithm.datadefinitions.DataDefinitions
import com.predictionalgorithm.datadefinitions.tfl.loadresources._
import com.predictionalgorithm.datadefinitions.tools.FetchPolyLines
import org.json4s.native.JsonMethods._

case class StopDefinitionFields(stopPointName:String, stopPointType:String, towards:String, bearing:Int, stopPointIndicator:String, stopPointState:Int, latitude:String, longitude:String)

/**
 * The reference for definitions files
 */
object TFLDefinitions extends DataDefinitions{

  override lazy val RouteDefinitionMap:Map[(String, Int), List[(Int, String, Option[String], String)]] =  LoadRouteDefinitions.getRouteDefinitionMap
  override  lazy val PointDefinitionsMap: Map[String,StopDefinitionFields] = LoadStopDefinitions.getStopDefinitionMap
  lazy val RouteIgnoreList: Set[String] = LoadRouteIgnoreList.routeIgnoreSet
  lazy val StopIgnoreList: Set[String] = LoadStopIgnoreList.stopIgnoreSet
  lazy val PublicHolidayList:List[Date] = LoadPublicHolidayList.publicHolidayList

  lazy val RouteList = getRouteList

  def updateRouteDefinitionsFromWeb() = {
    LoadRouteDefinitions.updateFromWeb()
  }

  def updateStopDefinitionsFromWeb() = {
    LoadStopDefinitions.updateFromWeb()
  }

  def addPolyLinesFromWeb() = {
    FetchPolyLines.updateAll()
  }

  /**
   * This gets the Route List from the Route Definiiton Map. As there are a mix of numbers and letters, the numbers and letters are partitioned, sorted separately and then joined
   * @return A list of sorted Routes
   */
  private def getRouteList:List[String] = {
    val list = TFLDefinitions.RouteDefinitionMap.map(x => x._1._1).toSet.toList
    val partitionedList = list.partition(x => !x.charAt(0).isLetter)
    val sortedIntList = partitionedList._1.map(_.toInt).sorted
    val sortedStringList = partitionedList._2.sorted
    sortedIntList.map(_.toString) ++ sortedStringList
  }
}