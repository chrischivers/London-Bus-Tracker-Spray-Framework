package com.PredictionAlgorithm.DataDefinitions

import scala.collection.immutable.ListMap

/**
 * Created by chrischivers on 17/07/15.
 */
trait LoadRouteDefinitionsInterface extends LoadResource{


  // Map format = Route_ID, Direction_ID, BusStopCode, First_Last -> pointsSequence
  var sequenceMap: Map[(String, Int, String), (Int, Option[String])] = ListMap()

  def getMap:Map[(String, Int, String), (Int, Option[String])] = sequenceMap

}
