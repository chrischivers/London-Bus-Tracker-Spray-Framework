package com.predictionalgorithm.database

import akka.actor.{ActorRef, ActorSystem}
import com.predictionalgorithm.database.tfl.{TFLInsertPointToPointDurationSupervisor$, TFLMongoDBConnection}

import scala.util.{Failure, Success, Try}

/**
 * Interface for insertions to the database
 */
trait DatabaseInsert extends DatabaseTransaction{


  protected val dbTransactionActor:ActorRef
  @volatile var numberDBTransactionsRequested: Long = 0
  @volatile var numberDBTransactionsExecuted: Long = 0

  def insertDocument(doc: DatabaseDocuments) = {
      dbTransactionActor ! doc
      numberDBTransactionsRequested += 1
  }
}
