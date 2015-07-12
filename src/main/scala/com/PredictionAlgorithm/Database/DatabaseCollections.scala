package com.PredictionAlgorithm.Database


/**
 * Created by chrischivers on 20/06/15.
 */
sealed trait DatabaseCollections {
  val name: String
  val fieldsVector: Vector[String]
}

final case object POINT_TO_POINT_COLLECTION extends DatabaseCollections {


val ROUTE_ID = "ROUTE_ID"
val DIRECTION_ID = "DIRECTION_ID"
val FROM_POINT_ID = "FROM_POINT_ID"
val TO_POINT_ID = "TO_POINT_ID"
val DAY_TYPE = "DAY_TYPE"
val DEP_TIME = "DEP_TIME"
val DURATION = "DURATION"
val INSERT_TIMESTAMP = "INSERT_TIMESTAMP"

  override val name: String = "PointToPoint"
  override val fieldsVector = Vector(ROUTE_ID, DIRECTION_ID, FROM_POINT_ID, TO_POINT_ID, DAY_TYPE, DEP_TIME, DURATION, INSERT_TIMESTAMP)

}
