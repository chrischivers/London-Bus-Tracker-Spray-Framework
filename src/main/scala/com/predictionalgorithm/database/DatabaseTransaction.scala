package com.predictionalgorithm.database

import akka.actor.{Actor, ActorRef, ActorSystem}
import com.predictionalgorithm.database.tfl.TFLMongoDBConnection

import scala.util.{Failure, Success, Try}


trait DatabaseTransaction {

  protected implicit val actorSystem = ActorSystem("DB_Actor_System")
  protected val collection:DatabaseCollections

  lazy val dBCollection =
    Try(TFLMongoDBConnection.getCollection(collection)) match {
      case Success(coll) => coll
      case Failure(fail) => throw new IllegalStateException("Cannot get DB Collection "+ fail)
    }


}
