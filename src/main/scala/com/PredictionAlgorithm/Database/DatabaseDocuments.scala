package com.PredictionAlgorithm.Database

sealed trait DatabaseDocuments {
  val collection: DatabaseCollections

}

final case class POINT_TO_POINT_DOCUMENT(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Of_Week: String, timeOffsetSeconds: Int, durationSeconds: Int) extends DatabaseDocuments {

  override val collection = POINT_TO_POINT_COLLECTION

}

final case class ROUTE_DEFINITION_DOCUMENT(route_ID: String, direction_ID: Int, sequence: Int, stop_Code: String, first_Last: Option[String]) extends DatabaseDocuments {

  override val collection = ROUTE_DEFINITIONS_COLLECTION

}

final case class STOP_DEFINITION_DOCUMENT(stopCode: String, stopName:String, stopType: String, towards: String, bearing:Int, indicator:String, state:Int, lat:Double, lng: Double) extends DatabaseDocuments {

  override val collection = STOP_DEFINITIONS_COLLECTION

}