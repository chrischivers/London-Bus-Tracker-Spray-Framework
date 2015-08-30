package com.predictionalgorithm.database.tfl

import akka.actor.{Actor, ActorRef, Props}
import com.predictionalgorithm.database._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Deletes a PointToPointDuration asyncronously
 */
object TFLDeletePointToPointDuration extends DatabaseDeleteInterface {

  override val dbDeleteActor: ActorRef = actorSystem.actorOf(Props[TFLDeletePointToPointDuration], name = "TFLDeletePointToPointDurationActor")

  override protected val collection: DatabaseCollections = POINT_TO_POINT_COLLECTION
}



class TFLDeletePointToPointDuration extends Actor {

  val collection = POINT_TO_POINT_COLLECTION

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

