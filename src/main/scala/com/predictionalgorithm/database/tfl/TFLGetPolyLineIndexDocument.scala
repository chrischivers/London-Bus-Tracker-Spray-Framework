package com.predictionalgorithm.database.tfl

import com.predictionalgorithm.database.{POLYLINE_INDEX_COLLECTION, DatabaseCollections, DatabaseQuery}

/**
 * Gets a PolyLineIndex Document
 */
object TFLGetPolyLineIndexDocument extends  DatabaseQuery {
  override protected val collection: DatabaseCollections = POLYLINE_INDEX_COLLECTION

}
