package com.PredictionAlgorithm.Database

import akka.actor.{ActorRef, ActorSystem}
import com.PredictionAlgorithm.Database.TFL.TFLMongoDBConnection
import org.bson.types.ObjectId

import scala.util.{Failure, Success, Try}

trait DatabaseDeleteInterface {

  protected implicit val actorSystem = ActorSystem("DB_Actor_System")
  protected val dbDeleteActor:ActorRef

  protected val collection:DatabaseCollections

  lazy val dBCollection =
    Try(TFLMongoDBConnection.getCollection(collection)) match {
      case Success(coll) => coll
      case Failure(fail) => throw new IllegalStateException("Cannot get DB Collection "+ fail)
    }

  def deleteDocument(docID: ObjectId): Unit = {
    dbDeleteActor ! docID
  }

}
