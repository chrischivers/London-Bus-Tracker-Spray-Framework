package com.predictionalgorithm.database.tfl

import akka.actor.{Actor, ActorRef, Props}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.predictionalgorithm.database._

/**
 * Deletes a PointToPointDuration asyncronously
 */
object TFLDeletePointToPointDuration extends DatabaseDelete {

  override protected val collection: DatabaseCollections = POINT_TO_POINT_COLLECTION
  override protected val dbTransactionActor: ActorRef = actorSystem.actorOf(Props[TFLDeletePointToPointDuration], name = "TFLDeletePointToPointDurationActor")
}

class TFLDeletePointToPointDuration extends Actor {

  override def receive: Receive = {
    case docID: ObjectId => deleteFromDB(docID)
    case _ => throw new IllegalStateException("TFL Delete Point Actor received unknown message")
  }


  private def deleteFromDB(docID: ObjectId) = {

   val query = MongoDBObject(
     "_id" -> docID)

    val writeConcern = TFLDeletePointToPointDuration.dBCollection.remove(query)
    assert(writeConcern.getN == 1)
  }
}

