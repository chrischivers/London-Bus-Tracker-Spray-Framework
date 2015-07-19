package com.PredictionAlgorithm.DataDefinitions.TFL


object TFLRouteDefinitions {

  //lazy val TFLSequenceMap:Map[(String, Int, String), (Int, Option[String])] = LoadRouteDefinitionsFromWebsite.getMap
  lazy val StopToPointSequenceMap:Map[(String, Int, String), (Int, Option[String])] = LoadRouteDefinitionsFromFile.getStopToPointSequenceMap
  lazy val PointToStopSequenceMap:Map[(String, Int, Int), (String, Option[String])] = LoadRouteDefinitionsFromFile.getPointToStopSequenceMap
  lazy val RouteIgnoreList:Set[String] = LoadRouteIgnoreListFromFile.routeIgnoreSet
  lazy val StopIgnoreList:Set[String] = LoadStopIgnoreListFromFile.stopIgnoreSet


}