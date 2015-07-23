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

  val collection = POINT_TO_POINT_COLLECTION

  val PRUNE_THRESHOLD_K_LIMIT = 10
  val PRUNE_THRESHOLD_TIME_LIMIT = 3600

  override def receive: Receive = {
    case doc1: POINT_TO_POINT_DOCUMENT => insertToDB(doc1)
    case _ => throw new IllegalStateException("TFL Insert Point Actor received unknown message")
  }


  private def insertToDB(doc: POINT_TO_POINT_DOCUMENT) = {

    val newObj = MongoDBObject(
      collection.ROUTE_ID -> doc.route_ID,
      collection.DIRECTION_ID -> doc.direction_ID,
      collection.FROM_POINT_ID -> doc.from_Point_ID,
      collection.TO_POINT_ID -> doc.to_Point_ID,
      collection.DAY -> doc.day_Of_Week)

    pruneExistingCollectionBeforeInsert(newObj, doc.timeOffsetSeconds)

    val pushUpdate = $push(collection.DURATION_LIST -> (MongoDBObject(collection.DURATION -> doc.durationSeconds, collection.TIME_OFFSET -> doc.timeOffsetSeconds, collection.TIME_STAMP -> System.currentTimeMillis())))

    // Upsert - pushing Duration and ObservedTime to Array
    TFLInsertPointToPointDuration.dBCollection.update(newObj, pushUpdate, upsert = true)
    TFLInsertPointToPointDuration.numberDBTransactionsExecuted += 1

  }

//TODO reduce overheads on this - maybe using _id
    def pruneExistingCollectionBeforeInsert(newObj: MongoDBObject,timeOffSet:Int): Unit = {
      val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(newObj)
      if (cursor.size > 0) {
        //If no entry in DB with route, direction, fromPoint and toPoint... do nothing
        assert(cursor.length == 1)
        val sortedDurTimeDifVec = getDurListVectorFromCursor(cursor.next(), timeOffSet) //First and only document in cursor
        val vecWithKNNTimeFiltering = sortedDurTimeDifVec.filter(_._4 <= PRUNE_THRESHOLD_TIME_LIMIT)
        if (vecWithKNNTimeFiltering.size > PRUNE_THRESHOLD_K_LIMIT) {
          val entryToDelete = vecWithKNNTimeFiltering.minBy(_._3) //Gets the oldest record in the vector
          val updatePull = $pull(collection.DURATION_LIST -> (MongoDBObject(collection.DURATION-> entryToDelete._1,collection.TIME_OFFSET -> entryToDelete._2, collection.TIME_STAMP -> entryToDelete._3)))
          TFLInsertPointToPointDuration.dBCollection.update(newObj, updatePull)
          println("pruning conducted")
        }
      }
    }


    // Vector is Duration, Time Offset, Time_Stamp, Time Offset Difference
    def getDurListVectorFromCursor(dbObject: Imports.MongoDBObject, timeOffSet:Int): Vector[(Int, Int, Long, Int)] = {
      dbObject.get(collection.DURATION_LIST).get.asInstanceOf[Imports.BasicDBList].map(y => {
        (y.asInstanceOf[Imports.BasicDBObject].getInt(collection.DURATION),
          y.asInstanceOf[Imports.BasicDBObject].getInt(collection.TIME_OFFSET),
          y.asInstanceOf[Imports.BasicDBObject].getLong(collection.TIME_STAMP),
          math.abs(y.asInstanceOf[Imports.BasicDBObject].getInt(collection.TIME_OFFSET) - timeOffSet))
      })
        .toVector
        .sortBy(_._4)
    }
}

