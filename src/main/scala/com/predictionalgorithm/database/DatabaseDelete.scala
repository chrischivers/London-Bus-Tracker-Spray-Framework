package com.predictionalgorithm.database

import akka.actor.{ActorRef, ActorSystem}
import com.predictionalgorithm.database.tfl.TFLMongoDBConnection
import org.bson.types.ObjectId

import scala.util.{Failure, Success, Try}

/**
 * Interface for deletions from the database
 */
trait DatabaseDelete extends DatabaseTransaction{

  protected implicit val actorSystem = ActorSystem("DB_Actor_System")
  protected val dbTransactionActor:ActorRef

  def deleteDocument(docID: ObjectId): Unit = {
    dbTransactionActor ! docID
  }

}
