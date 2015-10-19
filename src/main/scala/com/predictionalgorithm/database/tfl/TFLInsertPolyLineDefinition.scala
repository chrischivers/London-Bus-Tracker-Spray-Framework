package com.predictionalgorithm.database.tfl

import akka.actor.{Actor, Props, ActorRef}
import com.predictionalgorithm.database._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import com.typesafe.scalalogging.LazyLogging


object TFLInsertPolyLineDefinition extends DatabaseInsert{

  override protected val collection: DatabaseCollections = POLYLINE_INDEX_COLLECTION
  override val supervisor: ActorRef = actorDatabaseSystem.actorOf(Props[TFLInsertPolyLineDefinitionSupervisor], "TFLUpdatePolyLineSupervisor")
}

class TFLInsertPolyLineDefinitionSupervisor extends Actor {
  val dbTransactionActor: ActorRef = context.actorOf(Props[TFLInsertPolyLineDefinition], name = "TFLUpdatePolyLineActor")

  override def receive: Actor.Receive = {
    case doc1: POLYLINE_INDEX_DOCUMENT => dbTransactionActor ! doc1
  }
}

class TFLInsertPolyLineDefinition extends Actor with LazyLogging  {

  val collection = POLYLINE_INDEX_COLLECTION

  override def receive: Receive = {
    case doc1: POLYLINE_INDEX_DOCUMENT => insertToDB(doc1)
    case _ =>
      logger.error("TFL PolyLine Definition Actor received unknown message")
      throw new IllegalStateException("TFL PolyLine Definition Actor received unknown message")
  }


  private def insertToDB(doc: POLYLINE_INDEX_DOCUMENT) = {
    TFLInsertPolyLineDefinition.numberDBTransactionsRequested += 1

    val query = MongoDBObject(
      collection.FROM_STOP_CODE -> doc.fromStopCode,
      collection.TO_STOP_CODE -> doc.toStopCode)

    val update = $set(collection.POLYLINE -> doc.polyLine)

      TFLInsertPolyLineDefinition.dBCollection.update(query, update, upsert = true)
     TFLInsertPolyLineDefinition.numberDBTransactionsExecuted += 1
  }
}
