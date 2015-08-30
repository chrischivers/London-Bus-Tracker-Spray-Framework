package com.predictionalgorithm.database.tfl

import com.predictionalgorithm.database.{DatabaseCollections, POINT_TO_POINT_COLLECTION, DatabaseQueryInterface}

/**
 * Gets a PointToPointDuration asyncronously
 */
object TFLGetPointToPointDocument extends  DatabaseQueryInterface {

  override protected val collection: DatabaseCollections = POINT_TO_POINT_COLLECTION



}
