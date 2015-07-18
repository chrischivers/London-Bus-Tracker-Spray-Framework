package com.PredictionAlgorithm.Database.TFL


import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import com.PredictionAlgorithm.Database._
import com.PredictionAlgorithm.Processes.TFL.TFLIterateOverArrivalStream
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.mongodb.casbah.commons.ValidBSONType.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.query.Imports

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

/**
 * Created by chrischivers on 20/06/15.
 */
object TFLInsertPointToPointDuration extends DatabaseModifyInterface {

  override val dbModifyActor: ActorRef = actorSystem.actorOf(Props[TFLInsertPointToPointDuration], name = "TFLInsertPointToPointDurationActor")

  override lazy val dBCollection =
    Try(TFLMongoDBConnection.getCollection(POINT_TO_POINT_COLLECTION)) match {
      case Success(collection) => collection
      case Failure(fail) => throw new IllegalStateException("Cannot get DB Collection")
    }

  override def insertDocument(doc: DatabaseDocuments): Unit = {
    numberDBTransactionsRequested += 1
    dbModifyActor ! doc
  }


}

class TFLInsertPointToPointDuration extends Actor {

  override def receive: Receive = {
    case doc1: POINT_TO_POINT_DOCUMENT => insertToDB(doc1)
    case _ => throw new IllegalStateException("TFL Insert Point Actor received unknown message")
  }


  private def insertToDB(doc: POINT_TO_POINT_DOCUMENT) = {

    val collection = doc.collection
    val newObj = MongoDBObject(
      collection.ROUTE_ID -> doc.route_ID,
      collection.DIRECTION_ID -> doc.direction_ID,
      collection.FROM_POINT_ID -> doc.from_Point_ID,
      collection.TO_POINT_ID -> doc.to_Point_ID
     )

    val update1 = $push( collection.DAY_DURATIONS -> MongoDBObject(doc.day_Of_Week -> MongoDBObject(collection.DURATION_LIST -> (MongoDBObject(collection.DURATION -> doc.duration,collection.TIME_OFFSET -> doc.timeOffset)))))
    val update2 = $set(collection.LAST_UPDATED -> System.currentTimeMillis())

    // Upsert - pushing Duration and ObservedTime to Array
    TFLInsertPointToPointDuration.dBCollection.update(newObj,update1,upsert=true)
    TFLInsertPointToPointDuration.dBCollection.update(newObj,update2,upsert=true)
    TFLInsertPointToPointDuration.numberDBTransactionsExecuted += 1

  }
}

