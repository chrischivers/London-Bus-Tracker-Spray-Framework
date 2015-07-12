package com.PredictionAlgorithm.Database.TFL


import com.PredictionAlgorithm.Database.{POINT_TO_POINT_DOCUMENT, DatabaseDocuments, POINT_TO_POINT_COLLECTION, DatabaseModifyInterface}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.ValidBSONType.DBObject

import scala.util.{Failure, Success, Try}

/**
 * Created by chrischivers on 20/06/15.
 */
object TFLInsertPointToPointDuration extends DatabaseModifyInterface {

  override lazy val dBCollection =
    Try(TFLMongoDBConnection.getCollection(POINT_TO_POINT_COLLECTION)) match {
      case Success(collection) => collection
      case Failure(fail) => throw new IllegalStateException("Cannot get DB Collection")

    }

  override def insertDocument(doc: DatabaseDocuments): Unit =  doc match {
    case d1: POINT_TO_POINT_DOCUMENT => insert(d1)
    case _ => throw new ClassCastException
  }

      def insert(doc: POINT_TO_POINT_DOCUMENT) = {

      val collection = doc.collection
      val newObj = MongoDBObject(
        collection.ROUTE_ID -> doc.route_ID,
        collection.DIRECTION_ID -> doc.direction_ID,
        collection.FROM_POINT_ID -> doc.from_Point_ID,
        collection.TO_POINT_ID -> doc.to_Point_ID,
        collection.DAY_TYPE -> doc.day_Type,
        collection.DEP_TIME -> doc.dep_Time,
        collection.DURATION -> doc.duration,
        collection.INSERT_TIMESTAMP -> doc.insert_TimeStamp)
      dBCollection.insert(newObj)
  }
}

