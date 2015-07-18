package com.PredictionAlgorithm.Prediction

import com.PredictionAlgorithm.Database.{DatabaseCollections, POINT_TO_POINT_COLLECTION}
import com.mongodb.casbah.MongoCollection

/**
 * Created by chrischivers on 18/07/15.
 */
trait PredictionInterface {

  val coll:DatabaseCollections

  def makePrediction(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Of_Week: String, timeOffset: Int):Option[Int]

}
