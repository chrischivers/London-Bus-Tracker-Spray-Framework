package com.predictionalgorithm.database.tfl


import akka.actor.{Actor, Props}
import akka.routing.RoundRobinPool
import com.predictionalgorithm.database._
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.{Imports, MongoDBObject}
import com.mongodb.casbah.Imports._


case class PruneRequest(mongoObj: Imports.DBObject, timeOffSet: Int, rainfall: Double)
case class PruneCompleted()

object TFLInsertPointToPointDurationSupervisor extends DatabaseInsert {
  val collection = POINT_TO_POINT_COLLECTION
  override val supervisor = actorSystem.actorOf(Props[TFLInsertPointToPointDurationSupervisor], "InsertPointToPointSupervisor")

  @volatile var numberDBPullTransactionsRequested: Long = 0
  @volatile var numberDBPullTransactionsExecuted: Long = 0

}

class  TFLInsertPointToPointDurationSupervisor extends Actor {


  val insertRouter = context.actorOf(RoundRobinPool(15).props(Props[TFLInsertPointToPointDurationActor]), "InsertPointToPointRouter")
  val pruneRouter = context.actorOf(RoundRobinPool(15).props(Props[TFLPrunePointToPointActor]), "PrunePointToPointRouter")


  override def receive = {
    case doc: DatabaseDocument => insertDoc(doc)
    case po: PruneRequest => pruneDatabaseArray(po)
    case Completed => TFLInsertPointToPointDurationSupervisor.numberDBTransactionsExecuted += 1
    case PruneCompleted => TFLInsertPointToPointDurationSupervisor.numberDBPullTransactionsExecuted += 1
  }


  def insertDoc(doc: DatabaseDocument): Unit = {
    if (TFLInsertPointToPointDurationSupervisor.numberDBTransactionsRequested - TFLInsertPointToPointDurationSupervisor.numberDBTransactionsExecuted < TFLInsertPointToPointDurationSupervisor.MAXIMUM_OUTSTANDING_TRANSACTIONS) {
      insertRouter ! doc
      TFLInsertPointToPointDurationSupervisor.numberDBTransactionsRequested += 1
    }
    else TFLInsertPointToPointDurationSupervisor.numberDBTransactionsDroppedDueToOverflow += 1
  }

  def pruneDatabaseArray(pruneObj: PruneRequest) = {
    TFLInsertPointToPointDurationSupervisor.numberDBPullTransactionsRequested += 1
    pruneRouter ! pruneObj
  }

}

class TFLInsertPointToPointDurationActor extends Actor {

  val collection = TFLInsertPointToPointDurationSupervisor.collection
  val dbCollection = TFLInsertPointToPointDurationSupervisor.dBCollection

  override def receive: Receive = {
    case doc1: POINT_TO_POINT_DOCUMENT => insertToDB(doc1)
    case e => throw new IllegalStateException("TFL Insert Point Actor received unknown message. Message: " + e)
  }


  private def insertToDB(doc: POINT_TO_POINT_DOCUMENT) = {

    val newObj = MongoDBObject(
      collection.ROUTE_ID -> doc.route_ID,
      collection.DIRECTION_ID -> doc.direction_ID,
      collection.FROM_POINT_ID -> doc.from_Point_ID,
      collection.TO_POINT_ID -> doc.to_Point_ID,
      collection.DAY -> doc.day_Of_Week)

    val pushUpdate = $push(collection.DURATION_LIST -> MongoDBObject(collection.DURATION -> doc.durationSeconds, collection.TIME_OFFSET -> doc.timeOffsetSeconds, collection.RAINFALL -> doc.rainfall, collection.TIME_STAMP -> System.currentTimeMillis()))
    val update = dbCollection.update(newObj, pushUpdate, upsert = true)
    TFLInsertPointToPointDurationSupervisor.supervisor ! Completed
    if (update.isUpdateOfExisting) {
      TFLInsertPointToPointDurationSupervisor.supervisor ! new PruneRequest(newObj, doc.timeOffsetSeconds, doc.rainfall)
    }


  }
}
class TFLPrunePointToPointActor extends Actor {

  val collection = POINT_TO_POINT_COLLECTION
  
  val PRUNE_THRESHOLD_K_LIMIT = 10
  val PRUNE_THRESHOLD_TIME_LIMIT = 1800
  val PRUNE_THRESHOLD_RAINFALL_LIMIT = 0.5

  override def receive: Actor.Receive = {
    case pruneObj: PruneRequest => pruneDBArray(pruneObj) 
    case _ => throw new IllegalStateException("TFL PruneActor received unknown message")
  }

  private def pruneDBArray(pruneObj: PruneRequest) = {

    val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(pruneObj.mongoObj)
    if (cursor.length == 1) {

      val record = cursor.next().asInstanceOf[Imports.BasicDBObject]

      val durListVector = getDurListVectorFromCursor(record)

      // This filters those within the PRUNE THRESHOLD LIMIT followed by those within the rainfall threshold
      val prunedVector = durListVector.filter(x =>
        math.abs(x._2 - pruneObj.timeOffSet) <= PRUNE_THRESHOLD_TIME_LIMIT &&
          math.abs(x._4 - pruneObj.rainfall) <= PRUNE_THRESHOLD_RAINFALL_LIMIT)

      if (prunedVector.size > PRUNE_THRESHOLD_K_LIMIT) {
        val entryToDelete = prunedVector.minBy(_._3) //Gets the oldest record in the vector
        val updatePull = $pull(collection.DURATION_LIST -> MongoDBObject(collection.DURATION -> entryToDelete._1, collection.TIME_OFFSET -> entryToDelete._2, collection.TIME_STAMP -> entryToDelete._3, collection.RAINFALL -> entryToDelete._4))

        TFLInsertPointToPointDurationSupervisor.dBCollection.update(pruneObj.mongoObj, updatePull)
      }
      TFLInsertPointToPointDurationSupervisor.supervisor ! PruneCompleted
    }
  }

  /*
   *
   * @param dbObject The database document
   * @return A vector of Duration, Time Offset, Time Stamp and Time Offset Difference
   */
  private def getDurListVectorFromCursor(dbObject: Imports.MongoDBObject): Vector[(Int, Int, Long, Double)] = {
    dbObject.get(collection.DURATION_LIST).get.asInstanceOf[Imports.BasicDBList].map(y => {
      (y.asInstanceOf[Imports.BasicDBObject].getInt(collection.DURATION),
        y.asInstanceOf[Imports.BasicDBObject].getInt(collection.TIME_OFFSET),
        y.asInstanceOf[Imports.BasicDBObject].getLong(collection.TIME_STAMP),
        y.asInstanceOf[Imports.BasicDBObject].getDouble(collection.RAINFALL))
    })
      .toVector
  }
}