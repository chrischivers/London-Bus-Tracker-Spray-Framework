package com.PredictionAlgorithm.Database

import akka.actor.{ActorRef, ActorSystem}
import com.PredictionAlgorithm.Database.TFL.TFLInsertPointToPointDuration._
import com.PredictionAlgorithm.Database.TFL.TFLMongoDBConnection
import com.mongodb.casbah.MongoCollection

import scala.util.{Failure, Success, Try}

trait DatabaseModifyInterface {

  protected implicit val actorSystem = ActorSystem("DB_Actor_System")
  protected val dbModifyActor:ActorRef

  protected val collection:DatabaseCollections

  lazy val dBCollection =
    Try(TFLMongoDBConnection.getCollection(collection)) match {
      case Success(coll) => coll
      case Failure(fail) => throw new IllegalStateException("Cannot get DB Collection "+ fail)
    }

  def insertDocument(doc: DatabaseDocuments): Unit = {
    numberDBTransactionsRequested += 1
    dbModifyActor ! doc
  }

}
