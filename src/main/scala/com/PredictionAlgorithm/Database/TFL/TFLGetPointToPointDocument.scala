package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.{DatabaseCollections, POINT_TO_POINT_COLLECTION, DatabaseQueryInterface}

/**
 * Gets a PointToPointDuration asyncronously
 */
object TFLGetPointToPointDocument extends  DatabaseQueryInterface {

  override protected val collection: DatabaseCollections = POINT_TO_POINT_COLLECTION



}
