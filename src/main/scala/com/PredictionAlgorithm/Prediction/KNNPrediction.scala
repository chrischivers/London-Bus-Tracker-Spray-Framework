package com.PredictionAlgorithm.Prediction

import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Database.TFL.TFLGetPointToPointDocument
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.{MongoDBList, Imports, MongoDBObject}
import scala.collection.JavaConversions._


object KNNPrediction extends PredictionInterface {

  override val coll = POINT_TO_POINT_COLLECTION

  override def makePrediction(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Of_Week: String, timeOffset: Int): Option[Int] = {

    val query = MongoDBObject(coll.ROUTE_ID -> route_ID, coll.DIRECTION_ID -> direction_ID, coll.FROM_POINT_ID -> from_Point_ID, coll.TO_POINT_ID -> to_Point_ID)


    val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(query)

    println(cursor.size)
    if (cursor.size == 0) return None
    else {
      var sum = 0
      var n = 0
      cursor.foreach(x => x.get(coll.DURATION_LIST).asInstanceOf[Imports.BasicDBList]
        .foreach(x => {
        sum += x.asInstanceOf[Imports.BasicDBObject].get(coll.DURATION).asInstanceOf[Int]
        n += 1
      }))
      Option(sum / n) //Calculate Average
    }


  }
}
