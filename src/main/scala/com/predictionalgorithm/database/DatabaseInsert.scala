package com.predictionalgorithm.database

import akka.actor.{ActorRef, ActorSystem}
import com.predictionalgorithm.database.tfl.TFLMongoDBConnection

import scala.util.{Failure, Success, Try}

/**
 * Interface for insertions to the database
 */
trait DatabaseInsert extends DatabaseTransaction{

  protected implicit val actorSystem = ActorSystem("DB_Actor_System")
  protected val dbTransactionActor:ActorRef

  def insertDocument(doc: DatabaseDocuments): Unit = {
    dbTransactionActor ! doc
  }


}
