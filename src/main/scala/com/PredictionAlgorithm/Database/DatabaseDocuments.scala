package com.PredictionAlgorithm.Database

sealed trait DatabaseDocuments {
  val collection: DatabaseCollections

}

final case class POINT_TO_POINT_DOCUMENT(val route_ID: String, val direction_ID: Int, val from_Point_ID: String, val to_Point_ID: String, val day_Type: String, val dep_Time: Long, val duration: Long, val insert_TimeStamp: Long) extends DatabaseDocuments {

  override val collection = POINT_TO_POINT_COLLECTION

}