package com.PredictionAlgorithm.DataDefinitions

import scala.collection.immutable.ListMap

/**
 * Created by chrischivers on 17/07/15.
 */
trait LoadRouteDefinitionsInterface extends LoadResource{


  // Map format = Route_ID, Direction_ID, BusStopCode, First_Last -> pointsSequence
  var StopToPointSequenceMap: Map[(String, Int, String), (Int, Option[String])] = ListMap()
  var PointToStopSequenceMap: Map[(String, Int, Int), (String, Option[String])] = ListMap()

  def getStopToPointSequenceMap:Map[(String, Int, String), (Int, Option[String])] = StopToPointSequenceMap
  def getPointToStopSequenceMap:Map[(String, Int, Int), (String, Option[String])] = PointToStopSequenceMap

}
