package com.predictionalgorithm.database.tfl

import com.predictionalgorithm.database.{DatabaseCollections, PREDICTION_DATABASE}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoCollection, MongoClient}

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

  def createIndex(mongoCollection: MongoCollection, dbc: DatabaseCollections) = {
    if (dbc.uniqueIndex) mongoCollection.createIndex(MongoDBObject(dbc.indexKeyList),MongoDBObject("unique" -> true))
    else mongoCollection.createIndex(MongoDBObject(dbc.indexKeyList),MongoDBObject("unique" -> false))
  }

}
