package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.{POINT_TO_POINT_COLLECTION, DatabaseCollections, PREDICTION_DATABASE, Databases}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoCollection, MongoDB, MongoClient}

object TFLMongoDBConnection {

  lazy val mc: MongoClient = MongoClient()

  lazy val getDatabase = mc(PREDICTION_DATABASE.name)

  def getCollection(dbc:DatabaseCollections) = {
    val x = getDatabase(dbc.name)
    createIndex(x, dbc)
    println(x.getIndexInfo)
    x
  }

  def closeConnection() = mc.close()

  def createIndex(mongoCollection: MongoCollection, dbc: DatabaseCollections) = mongoCollection.createIndex(MongoDBObject(dbc.indexKeyList))
  //TODO make index unique
}
