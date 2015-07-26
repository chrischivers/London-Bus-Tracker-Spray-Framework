package com.PredictionAlgorithm.Database

import akka.actor.{ActorRef, ActorSystem}
import com.mongodb.casbah.MongoCollection

trait DatabaseModifyInterface {

  protected implicit val actorSystem = ActorSystem()
  protected val dbModifyActor:ActorRef

  protected val dBCollection:MongoCollection

  @volatile var numberDBTransactionsRequested:Long= 0
  @volatile var numberDBTransactionsExecuted:Long = 0
  @volatile var numberDBPullTransactionsExecuted:Long = 0

  def insertDocument(doc: DatabaseDocuments)

}
