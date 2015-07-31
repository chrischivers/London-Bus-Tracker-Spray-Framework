package com.PredictionAlgorithm.Database

import akka.actor.ActorSystem
import com.PredictionAlgorithm.Database.TFL.TFLGetRouteDefinitionDocument._
import com.PredictionAlgorithm.Database.TFL.TFLMongoDBConnection
import com.mongodb.casbah.{MongoCursor, MongoCollection}
import com.mongodb.casbah.commons.{Imports, MongoDBObject}

import scala.util.{Failure, Success, Try}


trait DatabaseQueryInterface {
  protected implicit val actorSystem = ActorSystem()

  protected val collection:DatabaseCollections

  @volatile var numberDBQueriesRun:Long = 0

  lazy val dBCollection =
    Try(TFLMongoDBConnection.getCollection(collection)) match {
      case Success(coll) => coll
      case Failure(fail) => throw new IllegalStateException("Cannot get DB Collection")
    }

  def executeQuery(mongoObj: Imports.DBObject): MongoCursor = {
    dBCollection.find(mongoObj)
  }

  def fetchAll():MongoCursor= {
    dBCollection.find()
  }

}
