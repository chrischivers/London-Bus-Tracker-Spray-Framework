package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.{STOP_DEFINITIONS_COLLECTION, DatabaseCollections, DatabaseQueryInterface, ROUTE_DEFINITIONS_COLLECTION}
import com.mongodb.casbah.MongoCursor

/**
 * Created by chrischivers on 31/07/15.
 */
object TFLGetStopDefinitionDocument extends DatabaseQueryInterface{

  override protected val collection: DatabaseCollections = STOP_DEFINITIONS_COLLECTION

  def getDistinctStopCodes():Set[String] = {
    TFLMongoDBConnection.getCollection(ROUTE_DEFINITIONS_COLLECTION).distinct(ROUTE_DEFINITIONS_COLLECTION.POINT_ID).asInstanceOf[Seq[String]].toSet
  }


}
