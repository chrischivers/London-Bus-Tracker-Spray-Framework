package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.{POINT_TO_POINT_COLLECTION, DatabaseCollections, PREDICTION_DATABASE, Databases}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoCollection, MongoDB, MongoClient}

object TFLMongoDBConnection {

  lazy val mc: MongoClient = MongoClient()

  lazy val getDatabase = mc(PREDICTION_DATABASE.name)

  def getCollection(dbc:DatabaseCollections): MongoCollection = {
    val coll = getDatabase(dbc.name)
    createIndex(coll, dbc)
    println(coll.getIndexInfo)
    coll
  }

  def closeConnection() = mc.close()

  def createIndex(mongoCollection: MongoCollection, dbc: DatabaseCollections) = mongoCollection.createIndex(MongoDBObject(dbc.indexKeyList))
  //TODO make index unique
}
