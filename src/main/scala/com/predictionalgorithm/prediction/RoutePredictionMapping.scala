package com.predictionalgorithm.prediction

import com.predictionalgorithm.datadefinitions.tfl.TFLDefinitions

case class PredictionRequest(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Of_Week: String, timeOffset: Int)
case class RouteListVariables(pointSeq: Int, fromStop:String, toStop:String, duration:Double)

object RoutePredictionMapping {


  def getRoutePredictionMap(routeID: String, direction: Int, dayOfWeeK: String, timeOffset: Int):Option[List[RouteListVariables]] = {

    val routeList = TFLDefinitions.RouteDefinitionMap.get(routeID,direction)
    if (routeList.isEmpty) None

    else {
      val rl = routeList.get.sortBy(_._1)
      var tempList = List[RouteListVariables]()
      var cumulativeTimeOffset= timeOffset.toDouble
      for (a <- 0 until rl.length - 2) {
        val point = a + 1
        val from = rl(a)._2
        val to = rl(a + 1)._2
        val duration = KNNPrediction.makePredictionBetweenConsecutivePoints(new PredictionRequest(routeID,direction,from,to,dayOfWeeK,cumulativeTimeOffset.toInt))
        tempList = tempList :+ new RouteListVariables(point,from,to,duration.getOrElse(return None)._1)
        cumulativeTimeOffset += duration.get._1
      }
      Some(tempList)
    }
  }


  private def validateRouteSequenceComplete(sortedRouteSequence:List[(Int,String,Option[String])]):Boolean = {
    val finalPointNumber = sortedRouteSequence.length - 1

    if(sortedRouteSequence.head._3.getOrElse(return false) != "FIRST" && sortedRouteSequence(finalPointNumber)._3.getOrElse(return false) != "LAST" ) return false
    for(a <- 0 until finalPointNumber) {
      if(sortedRouteSequence(a)._1 + 1 != sortedRouteSequence(a + 1)._1) return false
    }
    true
  }

}
