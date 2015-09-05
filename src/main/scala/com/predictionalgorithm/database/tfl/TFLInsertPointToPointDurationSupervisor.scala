package com.predictionalgorithm.database.tfl


import akka.actor.{ActorRef, Props, Actor}
import akka.routing.RoundRobinPool
import com.predictionalgorithm.database._
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.{Imports, MongoDBObject}
import com.mongodb.casbah.Imports._


case class PruneObject(mongoObj: Imports.DBObject, timeOffSet: Int, rainfall: Double)
object TFLInsertPointToPointDurationSupervisor extends DatabaseInsert {

  @volatile var numberDBPullTransactionsRequested: Long = 0
  @volatile var numberDBPullTransactionsExecuted: Long = 0

  override protected val collection: DatabaseCollections = POINT_TO_POINT_COLLECTION
  val routerProps = actorSystem.actorOf(RoundRobinPool(10).props(Props[TFLInsertPointToPointDurationActor]), "InsertPointToPointRouter")
  override protected val dbTransactionActor: ActorRef = routerProps

  def pruneDatabaseArray(pruneObj: PruneObject) = {
    numberDBPullTransactionsRequested += 1
    dbTransactionActor ! pruneObj
  }
}

class TFLInsertPointToPointDurationActor extends Actor {

  val collection = POINT_TO_POINT_COLLECTION

  val PRUNE_THRESHOLD_K_LIMIT = 10
  val PRUNE_THRESHOLD_TIME_LIMIT = 1800
  val PRUNE_THRESHOLD_RAINFALL_LIMIT = 0.5


  override def receive: Receive = {
    case doc1: POINT_TO_POINT_DOCUMENT => insertToDB(doc1)
    case pruneObj: PruneObject => pruneDBArray(pruneObj)
    case _ => throw new IllegalStateException("TFL Insert Point Actor received unknown message")
  }


  private def insertToDB(doc: POINT_TO_POINT_DOCUMENT) = {

    val newObj = MongoDBObject(
      collection.ROUTE_ID -> doc.route_ID,
      collection.DIRECTION_ID -> doc.direction_ID,
      collection.FROM_POINT_ID -> doc.from_Point_ID,
      collection.TO_POINT_ID -> doc.to_Point_ID,
      collection.DAY -> doc.day_Of_Week)

    val pushUpdate = $push(collection.DURATION_LIST -> MongoDBObject(collection.DURATION -> doc.durationSeconds, collection.TIME_OFFSET -> doc.timeOffsetSeconds, collection.RAINFALL -> doc.rainfall, collection.TIME_STAMP -> System.currentTimeMillis()))
    val update = TFLInsertPointToPointDurationSupervisor.dBCollection.update(newObj, pushUpdate, upsert = true)
    TFLInsertPointToPointDurationSupervisor.numberDBTransactionsExecuted += 1
    if (update.isUpdateOfExisting) {
      TFLInsertPointToPointDurationSupervisor.pruneDatabaseArray(new PruneObject(newObj, doc.timeOffsetSeconds, doc.rainfall))
    }


  }

  private def pruneDBArray(pruneObj: PruneObject) = {

    val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(pruneObj.mongoObj)
    assert(cursor.length == 1) //Should always exist, with just one record

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
    TFLInsertPointToPointDurationSupervisor.numberDBPullTransactionsExecuted += 1
  }

  /*
   *
   * @param dbObject The database document
   * @return A vector of Duration, Time Offset, Time Stamp and Time Offset Difference
   */
  def getDurListVectorFromCursor(dbObject: Imports.MongoDBObject): Vector[(Int, Int, Long, Double)] = {
    dbObject.get(collection.DURATION_LIST).get.asInstanceOf[Imports.BasicDBList].map(y => {
      (y.asInstanceOf[Imports.BasicDBObject].getInt(collection.DURATION),
        y.asInstanceOf[Imports.BasicDBObject].getInt(collection.TIME_OFFSET),
        y.asInstanceOf[Imports.BasicDBObject].getLong(collection.TIME_STAMP),
        y.asInstanceOf[Imports.BasicDBObject].getDouble(collection.RAINFALL))
    })
      .toVector
  }
}