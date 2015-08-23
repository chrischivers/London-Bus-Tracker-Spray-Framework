package com.PredictionAlgorithm.Database

import akka.actor.ActorSystem
import com.PredictionAlgorithm.Database.TFL.TFLMongoDBConnection
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.Imports

import scala.util.{Failure, Success, Try}


trait DatabaseQueryInterface {
  protected implicit val actorSystem = ActorSystem("DB_Actor_System")

  protected val collection:DatabaseCollections

  @volatile var numberDBQueriesRun:Long = 0

  lazy val dBCollection =
    Try(TFLMongoDBConnection.getCollection(collection)) match {
      case Success(coll) => coll
      case Failure(fail) => throw new IllegalStateException("Cannot get DB Collection " + fail)
    }

  def executeQuery(mongoObj: Imports.DBObject): MongoCursor = {
    dBCollection.find(mongoObj)
  }

  def fetchAll():MongoCursor= {
    dBCollection.find()
  }

}
