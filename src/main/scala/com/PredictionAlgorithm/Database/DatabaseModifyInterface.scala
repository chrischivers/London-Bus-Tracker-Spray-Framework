package com.PredictionAlgorithm.Database

import akka.actor.{ActorRef, ActorSystem}
import com.mongodb.casbah.MongoCollection

trait DatabaseModifyInterface {

  implicit val actorSystem = ActorSystem()
  val dbModifyActor:ActorRef

  val dBCollection:MongoCollection

  @volatile var numberDBTransactionsRequested:Long= 0
  @volatile var numberDBTransactionsExecuted:Long = 0

  def insertDocument(doc: DatabaseDocuments)

}
