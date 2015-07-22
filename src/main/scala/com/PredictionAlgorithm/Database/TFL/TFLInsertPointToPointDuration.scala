package com.PredictionAlgorithm.Database.TFL


import akka.actor.{ActorRef, Props, Actor}
import com.PredictionAlgorithm.Database._
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.{Imports, MongoDBObject}
import com.mongodb.casbah.Imports._

import scala.util.{Failure, Success, Try}


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

  val PRUNE_THRESHOLD_K_LIMIT = 10
  val PRUNE_THRESHOLD_TIME_LIMIT = 3600

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
      collection.TO_POINT_ID -> doc.to_Point_ID,
      collection.DAY -> doc.day_Of_Week
    )


    def pruneExistingCollectionBeforeInsert(newObj: MongoDBObject): Unit = {
      val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(newObj)
      if (cursor.size > 0) {
        //If no entry in DB with route, direction, fromPoint and toPoint... do nothing
        assert(cursor.length == 1)
        val sortedDurTimeDifVec = getDurTimeDifVecFromCursor(cursor.next())
        val vecWithKNNTimeFiltering = sortedDurTimeDifVec.filter(_._3 <= PRUNE_THRESHOLD_TIME_LIMIT)
        if (vecWithKNNTimeFiltering.size > PRUNE_THRESHOLD_K_LIMIT) {
          val entryToDelete = vecWithKNNTimeFiltering.maxBy(_._3)
          val updatepull = $pull(collection.DURATION_LIST -> (MongoDBObject(collection.DURATION-> entryToDelete._1,collection.TIME_OFFSET -> entryToDelete._2)))
          TFLInsertPointToPointDuration.dBCollection.update(newObj, updatepull)
          println("pruning conducted")
        }
      }
    }


    // Vector is Duration, Time Offset, Time Offset Difference
    def getDurTimeDifVecFromCursor(dbObject: Imports.MongoDBObject): Vector[(Int, Int, Int)] = {
      dbObject.get(collection.DURATION_LIST).get.asInstanceOf[Imports.BasicDBList].map(y => {
        (y.asInstanceOf[Imports.BasicDBObject].getInt(collection.DURATION),
          y.asInstanceOf[Imports.BasicDBObject].getInt(collection.TIME_OFFSET),
          math.abs(y.asInstanceOf[Imports.BasicDBObject].getInt(collection.TIME_OFFSET) - doc.timeOffsetSeconds))
      })
        .toVector
        .sortBy(_._3)

    }

    pruneExistingCollectionBeforeInsert(newObj)

    val update1 = $push(collection.DURATION_LIST -> (MongoDBObject(collection.DURATION -> doc.durationSeconds, collection.TIME_OFFSET -> doc.timeOffsetSeconds)))
    val update2 = $set(collection.LAST_UPDATED -> System.currentTimeMillis())

    // Upsert - pushing Duration and ObservedTime to Array
    TFLInsertPointToPointDuration.dBCollection.update(newObj, update1, upsert = true)
    TFLInsertPointToPointDuration.dBCollection.update(newObj, update2, upsert = true)
    TFLInsertPointToPointDuration.numberDBTransactionsExecuted += 1
  }
}

