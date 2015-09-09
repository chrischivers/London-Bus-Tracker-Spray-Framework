package com.predictionalgorithm.datadefinitions

import akka.actor.ActorSystem

import scala.io.{BufferedSource, Source}


trait ResourceOperations {

  val actorResourcesSystem = ActorSystem("ResourcesActorSystem")

  val DEFAULT_ROUTE_LIST_FILE = Source.fromURL(getClass.getResource("/routeList.csv"))
  val DEFAULT_ROUTE_DEF_FILE = Source.fromURL(getClass.getResource("/busSequences.csv"))
  val DEFAULT_ROUTE_IGNORE_LIST_FILE = Source.fromURL(getClass.getResource("/routeIgnoreList.csv"))
  val DEFAULT_STOP_IGNORE_LIST_FILE = Source.fromURL(getClass.getResource("/stopIgnoreList.csv"))
  val DEFAULT_LOAD_USING_HTML_METHOD_FILE = Source.fromURL(getClass.getResource("/routesToGetUsingHTMLMethod.csv"))
  val DEFAULT_PUBLIC_HOLIDAY_LIST_FILE = Source.fromURL(getClass.getResource("/publicHolidayList.csv"))

}
