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
  val MAXIMUM_OUTSTANDING_TRANSACTIONS = 1000

  def insertDocument(doc: DatabaseDocuments) = {
    if (numberDBTransactionsRequested - numberDBTransactionsExecuted < MAXIMUM_OUTSTANDING_TRANSACTIONS) {
      dbTransactionActor ! doc
      numberDBTransactionsRequested += 1
    } else {
      println ("Insert request not being processed - too many outstanding tranactions")
    }
  }
}
