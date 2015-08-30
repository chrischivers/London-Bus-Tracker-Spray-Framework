package com.predictionalgorithm.database.tfl

import com.predictionalgorithm.database.{ROUTE_DEFINITIONS_COLLECTION, DatabaseCollections, DatabaseQueryInterface}
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.MongoDBObject

import scala.util.{Failure, Success, Try}

/**
 * Gets a TFL Route Definition Document asyncronously
 */
object TFLGetRouteDefinitionDocument extends DatabaseQueryInterface{

  override protected val collection: DatabaseCollections = ROUTE_DEFINITIONS_COLLECTION

  def fetchAllOrdered():MongoCursor= {
    dBCollection.find().sort(MongoDBObject(
      ROUTE_DEFINITIONS_COLLECTION.ROUTE_ID -> 1,
      ROUTE_DEFINITIONS_COLLECTION.DIRECTION_ID -> 1,
      ROUTE_DEFINITIONS_COLLECTION.SEQUENCE -> 1))
  }


}
