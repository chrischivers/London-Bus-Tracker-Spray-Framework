package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.{POINT_TO_POINT_COLLECTION, DatabaseQueryInterface}
import com.mongodb.casbah.{MongoCursor}
import com.mongodb.casbah.commons.{Imports, MongoDBObject}

import scala.util.{Failure, Success, Try}

object TFLGetPointToPointDocument extends  DatabaseQueryInterface {

  override lazy val dBCollection =
    Try(TFLMongoDBConnection.getCollection(POINT_TO_POINT_COLLECTION)) match {
      case Success(collection) => collection
      case Failure(fail) => throw new IllegalStateException("Cannot get DB Collection")
    }

  override def executeQuery(mongoObj: Imports.DBObject): MongoCursor = {
    dBCollection.find(mongoObj)
  }
}
