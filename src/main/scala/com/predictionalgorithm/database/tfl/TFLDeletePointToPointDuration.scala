package com.predictionalgorithm.database.tfl

import akka.actor.{Actor, ActorRef, Props}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.predictionalgorithm.database._
import com.predictionalgorithm.database.tfl.TFLInsertStopDefinition._
import grizzled.slf4j.Logger

/**
 * Deletes a PointToPointDuration asyncronously
 */
object TFLDeletePointToPointDuration extends DatabaseDelete {



  protected val collection: DatabaseCollections = POINT_TO_POINT_COLLECTION

  override val supervisor: ActorRef = actorDatabaseSystem.actorOf(Props[TFLDeletePointToPointDuration], "TFLDeletePointToPointDurationActor")
}

class TFLDeletePointToPointDurationSupervisor extends Actor {

  val dbTransactionActor: ActorRef = context.actorOf(Props[TFLDeletePointToPointDuration], name = "TFLDeletePointToPointDurationActor")

  override def receive: Actor.Receive = {
    case docID: ObjectId => dbTransactionActor ! docID
  }

}

class TFLDeletePointToPointDuration extends Actor {

  val logger = Logger[this.type]

  override def receive: Receive = {
    case docID: ObjectId => deleteFromDB(docID)
    case _ =>
      logger.error("TFL Delete Point Actor received unknown message")
      throw new IllegalStateException("TFL Delete Point Actor received unknown message")
  }


  private def deleteFromDB(docID: ObjectId) = {

   val query = MongoDBObject(
     "_id" -> docID)

    val writeConcern = TFLDeletePointToPointDuration.dBCollection.remove(query)
    assert(writeConcern.getN == 1)
  }
}

