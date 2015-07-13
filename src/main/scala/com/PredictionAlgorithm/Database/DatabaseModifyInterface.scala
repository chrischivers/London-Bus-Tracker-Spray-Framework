package com.PredictionAlgorithm.Database

import akka.actor.{ActorRef, ActorSystem}
import com.mongodb.casbah.MongoCollection

trait DatabaseModifyInterface {

  implicit val actorSystem = ActorSystem()
  val dbModifyActor:ActorRef

  val dBCollection:MongoCollection

  def insertDocument(doc: DatabaseDocuments)

}
