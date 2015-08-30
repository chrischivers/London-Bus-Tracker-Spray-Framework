package com.predictionalgorithm.database

import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.Imports
import com.predictionalgorithm.database.tfl.TFLGetPointToPointDocument._


/**
 * Interface for queries in the database
 */
trait DatabaseQuery extends DatabaseTransaction{

  @volatile var numberDBQueriesRun:Long = 0


  def executeQuery(mongoObj: Imports.DBObject): MongoCursor = {
    dBCollection.find(mongoObj)
  }

  def fetchAll():MongoCursor= {
    dBCollection.find()
  }
}
