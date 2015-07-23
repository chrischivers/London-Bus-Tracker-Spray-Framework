package com.PredictionAlgorithm.Database


/**
 * Created by chrischivers on 20/06/15.
 */
sealed trait DatabaseCollections {
  val name: String
  val fieldsVector: Vector[String]
  val indexKeyList: List[(String,Int)]
}

final case object POINT_TO_POINT_COLLECTION extends DatabaseCollections {


  val ROUTE_ID = "ROUTE_ID"
  val DIRECTION_ID = "DIRECTION_ID"
  val FROM_POINT_ID = "FROM_POINT_ID"
  val TO_POINT_ID = "TO_POINT_ID"
  val DAY = "DAY"
  val DURATION_LIST = "DURATION_LIST"
  val TIME_OFFSET = "TIME_OFFSET"
  val DURATION = "DURATION"
  val TIME_STAMP = "TIME_STAMP"

  override val name: String = "PointToPoint"
  override val fieldsVector = Vector(ROUTE_ID, DIRECTION_ID, FROM_POINT_ID, TO_POINT_ID, DAY, TIME_OFFSET, DURATION, TIME_STAMP)
  override val indexKeyList = List((ROUTE_ID, 1),(DIRECTION_ID, 1),(FROM_POINT_ID, 1),(TO_POINT_ID, 1), (DAY, 1))

}
