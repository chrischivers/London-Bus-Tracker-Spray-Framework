package com.PredictionAlgorithm.Database.TFL


import com.PredictionAlgorithm.Database.{POINT_TO_POINT_DOCUMENT, DatabaseDocuments, POINT_TO_POINT_COLLECTION, DatabaseModifyInterface}
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.mongodb.casbah.commons.ValidBSONType.DBObject
import com.mongodb.casbah.Imports._

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

      private def insert(doc: POINT_TO_POINT_DOCUMENT) = {

      val collection = doc.collection
      val newObj = MongoDBObject(
        collection.ROUTE_ID -> doc.route_ID,
        collection.DIRECTION_ID -> doc.direction_ID,
        collection.FROM_POINT_ID -> doc.from_Point_ID,
        collection.TO_POINT_ID -> doc.to_Point_ID,
        collection.DAY_TYPE -> doc.day_Type)
        //collection.UPDATED_TIMESTAMP -> System.currentTimeMillis())

        //dBCollection.insert(newObj)
        println("Inserting Point To Point Into DB")

        dBCollection.update(newObj,$push(collection.DURATION_LIST -> (MongoDBObject(collection.DURATION -> doc.duration,collection.OBSERVED_TIME -> doc.observed_Time))),upsert=true)

          //val newDocument: BasicDBObject = new BasicDBObject


        /* newLst: com.mongodb.BasicDBList = [ "foo" , "bar" , "x" , "y"] */
  }
}

