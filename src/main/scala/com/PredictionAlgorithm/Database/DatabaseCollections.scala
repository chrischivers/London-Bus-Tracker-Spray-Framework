package com.PredictionAlgorithm.Database

/**
 * Created by chrischivers on 20/06/15.
 */
sealed trait DatabaseCollections
{ val name: String }

final case object ARRIVAL_LOG_COLLECTION extends DatabaseCollections {
  override val name: String = "ArrivalLog"
}
