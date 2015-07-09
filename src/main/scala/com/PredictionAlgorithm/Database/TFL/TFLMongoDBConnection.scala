package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.Database.{DatabaseCollections, PREDICTION_DATABASE, Databases}
import com.PredictionAlgorithm.Database.MongoDB.MongoFactory
import com.mongodb.casbah.{MongoCollection, MongoDB, MongoClient}

class TFLMongoDBConnection extends MongoFactory{

  override var mc: MongoClient = getConnection

  def getDatabase(): MongoDB = getDatabase(mc,PREDICTION_DATABASE)

  def getCollection(collectionName: DatabaseCollections): MongoCollection = getCollection(getDatabase(),collectionName)

  def closeConnection() = mc.close()


}
