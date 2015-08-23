package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.{POLYLINE_INDEX_COLLECTION, DatabaseCollections, DatabaseQueryInterface}

/**
 * Gets a PolyLineIndex Document asyncronously
 */
object TFLGetPolyLineIndexDocument extends  DatabaseQueryInterface {
  override protected val collection: DatabaseCollections = POLYLINE_INDEX_COLLECTION
}
