package com.PredictionAlgorithm.DataDefinitions.TFL


object TFLRouteDefinitions {

  //lazy val TFLSequenceMap:Map[(String, Int, String), (Int, Option[String])] = LoadRouteDefinitionsFromWebsite.getMap
  lazy val TFLSequenceMap:Map[(String, Int, String), (Int, Option[String])] = LoadRouteDefinitionsFromFile.getMap
  lazy val RouteIgnoreList:Set[String] = LoadRouteIgnoreListFromFile.getSet
  lazy val StopIgnoreList:Set[String] = LoadStopIgnoreListFromFile.getSet


}