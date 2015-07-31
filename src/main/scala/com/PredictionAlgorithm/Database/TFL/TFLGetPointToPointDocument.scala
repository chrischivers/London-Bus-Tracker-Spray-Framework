package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.{ROUTE_DEFINITIONS_COLLECTION, DatabaseCollections, POINT_TO_POINT_COLLECTION, DatabaseQueryInterface}
import com.mongodb.casbah.{MongoCursor}
import com.mongodb.casbah.commons.{Imports, MongoDBObject}

import scala.util.{Failure, Success, Try}

object TFLGetPointToPointDocument extends  DatabaseQueryInterface {

  override protected val collection: DatabaseCollections = POINT_TO_POINT_COLLECTION



}
