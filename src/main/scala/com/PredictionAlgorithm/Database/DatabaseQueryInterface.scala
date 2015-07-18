package com.PredictionAlgorithm.Database

import akka.actor.ActorSystem
import com.mongodb.casbah.{MongoCursor, MongoCollection}
import com.mongodb.casbah.commons.{Imports, MongoDBObject}


trait DatabaseQueryInterface {
  protected implicit val actorSystem = ActorSystem()
  protected val dBCollection:MongoCollection

  @volatile var numberDBQueriesRun:Long = 0

  def executeQuery(mongoObj: Imports.DBObject):MongoCursor

}
