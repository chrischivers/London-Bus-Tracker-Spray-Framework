package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.TFL.TFLGetPointToPointDocument._
import com.PredictionAlgorithm.Database.{ROUTE_DEFINITIONS_COLLECTION, DatabaseCollections, POINT_TO_POINT_COLLECTION, DatabaseQueryInterface}
import com.mongodb.casbah.{MongoCollection, MongoCursor}
import com.mongodb.casbah.commons.{MongoDBObject, Imports}

import scala.util.{Failure, Success, Try}

/**
 * Created by chrischivers on 31/07/15.
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
