package com.predictionalgorithm.database.tfl

import com.predictionalgorithm.database.{DatabaseCollections, PREDICTION_DATABASE}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{WriteConcern, MongoCollection, MongoClient}
import grizzled.slf4j.Logger

object TFLMongoDBConnection {
  val logger = Logger[this.type]
  lazy val mc: MongoClient = MongoClient()

  lazy val getDatabase = mc(PREDICTION_DATABASE.name)

  def getCollection(dbc:DatabaseCollections): MongoCollection = {
    val coll = getDatabase(dbc.name)
    createIndex(coll, dbc)
    logger.info("Index Info: " + coll.getIndexInfo)
    coll
  }

  def closeConnection() = mc.close()

  def createIndex(mongoCollection: MongoCollection, dbc: DatabaseCollections) = {
    if (dbc.uniqueIndex) mongoCollection.createIndex(MongoDBObject(dbc.indexKeyList),MongoDBObject("unique" -> true))
    else mongoCollection.createIndex(MongoDBObject(dbc.indexKeyList),MongoDBObject("unique" -> false))
  }

}
