package com.predictionalgorithm.database.tfl

import com.predictionalgorithm.database.{DatabaseCollections, POINT_TO_POINT_COLLECTION, DatabaseQuery}

/**
 * Gets a PointToPointDuration
 */
object TFLGetPointToPointDocument extends  DatabaseQuery {

  override protected val collection: DatabaseCollections = POINT_TO_POINT_COLLECTION


}
