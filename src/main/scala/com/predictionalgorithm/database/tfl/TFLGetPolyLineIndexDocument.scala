package com.predictionalgorithm.database.tfl

import com.predictionalgorithm.database.{POLYLINE_INDEX_COLLECTION, DatabaseCollections, DatabaseQueryInterface}

/**
 * Gets a PolyLineIndex Document asyncronously
 */
object TFLGetPolyLineIndexDocument extends  DatabaseQueryInterface {
  override protected val collection: DatabaseCollections = POLYLINE_INDEX_COLLECTION
}
