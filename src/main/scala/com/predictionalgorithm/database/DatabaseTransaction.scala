package com.predictionalgorithm.database

import akka.actor.{Actor, ActorRef, ActorSystem}
import com.predictionalgorithm.Main
import com.predictionalgorithm.database.tfl.TFLMongoDBConnection
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

case class Completed()

trait DatabaseTransaction extends LazyLogging {

  protected val actorDatabaseSystem = ActorSystem("DatabaseSystem")
  val supervisor:ActorRef
  protected val collection:DatabaseCollections

  lazy val dBCollection =
    Try(TFLMongoDBConnection.getCollection(collection)) match {
      case Success(coll) => coll
      case Failure(fail) =>
        logger.error("Cannot get DB Collection ")
        throw new IllegalStateException("Cannot get DB Collection "+ fail)
    }


}
