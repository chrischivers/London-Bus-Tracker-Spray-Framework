package com.PredictionAlgorithm.Database

/**
 * Created by chrischivers on 20/06/15.
 */
trait DBFactoryInterface[A,B,C] {

  def getConnection: A

   def getDatabase(connection: A, dbName: Databases): B

  def getCollection(mongoDatabase: B, collectionName: DatabaseCollections): C

  def closeConnection(conn: A)


}
