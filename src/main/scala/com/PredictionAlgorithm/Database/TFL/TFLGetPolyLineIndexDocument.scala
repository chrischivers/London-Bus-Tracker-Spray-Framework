package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.{POLYLINE_INDEX_COLLECTION, DatabaseCollections, DatabaseQueryInterface}

/**
 * Created by chrischivers on 01/08/15.
 */
object TFLGetPolyLineIndexDocument extends  DatabaseQueryInterface {
  override protected val collection: DatabaseCollections = POLYLINE_INDEX_COLLECTION
}
