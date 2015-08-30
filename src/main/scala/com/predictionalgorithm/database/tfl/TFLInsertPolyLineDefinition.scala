package com.predictionalgorithm.database.tfl

import akka.actor.{Actor, Props, ActorRef}
import com.predictionalgorithm.database._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._


object TFLInsertPolyLineDefinition extends DatabaseInsertInterface{

  @volatile var numberPolyLinesInserted = 0

  override val dbInsertActor: ActorRef = actorSystem.actorOf(Props[TFLInsertPolyLineDefinition], name = "TFLUpdatePolyLine")
  override protected val collection: DatabaseCollections = POLYLINE_INDEX_COLLECTION
}

class TFLInsertPolyLineDefinition extends Actor {

  val collection = POLYLINE_INDEX_COLLECTION

  override def receive: Receive = {
    case doc1: POLYLINE_INDEX_DOCUMENT => insertToDB(doc1)
    case _ => throw new IllegalStateException("TFL PolyLine Definition Actor received unknown message")
  }


  private def insertToDB(doc: POLYLINE_INDEX_DOCUMENT) = {

    val query = MongoDBObject(
      collection.FROM_STOP_CODE -> doc.fromStopCode,
      collection.TO_STOP_CODE -> doc.toStopCode)

    val update = $set(collection.POLYLINE -> doc.polyLine)

      TFLInsertPolyLineDefinition.dBCollection.update(query, update, upsert = true)
     TFLInsertPolyLineDefinition.numberPolyLinesInserted += 1
  }
}
