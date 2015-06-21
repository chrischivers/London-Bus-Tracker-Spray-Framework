package com.PredictionAlgorithm.Database.MongoDB

import com.PredictionAlgorithm.Database.{DBFactoryInterface, DatabaseCollections, Databases}
import com.mongodb.casbah.{MongoClient, MongoCollection, MongoDB}


trait MongoFactory extends DBFactoryInterface[MongoClient, MongoDB, MongoCollection] {


  override def getConnection: MongoClient = return MongoClient()

  override def getDatabase(connection: MongoClient, dbName: Databases): MongoDB = connection(dbName.name)

  override def getCollection(mongoDatabase: MongoDB, collectionName: DatabaseCollections): MongoCollection = mongoDatabase(collectionName.name)

  override def closeConnection(conn: MongoClient) = conn.close

}
