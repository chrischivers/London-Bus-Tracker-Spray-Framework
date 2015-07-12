package com.PredictionAlgorithm.Database

import com.mongodb.casbah.MongoCollection

trait DatabaseModifyInterface {

  val dBCollection:MongoCollection

  def insertDocument(doc: DatabaseDocuments)


}
