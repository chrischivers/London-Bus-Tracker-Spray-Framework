package com.PredictionAlgorithm.Prediction

import com.PredictionAlgorithm.ControlInterface.QueryController
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions

case class PredictionRequest(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Of_Week: String, timeOffset: Int)
case class RouteListVariables(pointSeq: Int, fromStop:String, toStop:String, duration:Double)

object RoutePredictionMapping {

  private val predictionAlgorithm:PredictionInterface = KNNPrediction

  def getRoutePredictionMap(routeID: String, direction: Int, dayOfWeeK: String, timeOffset: Int):Option[List[RouteListVariables]] = {

    val routeList = getRouteList(routeID,direction)
    if (routeList.isEmpty) return None

    else {
      val rl = routeList.get
      var tempList = List[RouteListVariables]()
      var cumulativeTimeOffset= timeOffset.toDouble
      for (a <- 0 until rl.length - 2) {
        val point = a + 1
        val from = rl(a)._2
        val to = rl(a + 1)._2
        val duration = KNNPrediction.makePredictionBetweenConsecutivePoints(new PredictionRequest(routeID,direction,from,to,dayOfWeeK,cumulativeTimeOffset.toInt))
        tempList = tempList :+ new RouteListVariables(point,from,to,duration.getOrElse(return None))
        cumulativeTimeOffset += duration.get
      }
      Some(tempList)
    }
  }

  private def getRouteList(routeID: String, direction: Int): Option[List[(Int,String)]] = {
    //List is Route, Direction, PointSeq, Stop, First/Last
    val sortedRouteSequence = TFLDefinitions.RouteDirSequenceList.filter(x => x._1 == routeID && x._2 == direction).map(x => (x._3,x._4,x._5)).sortBy(_._1)
    if (!validateRouteSequenceComplete(sortedRouteSequence)) return None
    else Some(sortedRouteSequence.map(x => (x._1,x._2)))

  }

  private def validateRouteSequenceComplete(sortedRouteSequence:List[(Int,String,Option[String])]):Boolean = {
    val finalPointNumber = sortedRouteSequence.length - 1

    if(sortedRouteSequence(0)._3.getOrElse(return false) != "FIRST" && sortedRouteSequence(finalPointNumber)._3.getOrElse(return false) != "LAST" ) return false
    for(a <- 0 until finalPointNumber) {
      if(sortedRouteSequence(a)._1 + 1 != sortedRouteSequence(a + 1)._1) return false
    }
    true
  }

}
