package com.PredictionAlgorithm.Database.TFL

import com.PredictionAlgorithm.DataSource.Line
import com.PredictionAlgorithm.DataSource.TFL._
import com.PredictionAlgorithm.Database.DatabaseModifyInterface
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject

/**
 * Created by chrischivers on 20/06/15.
 */
case class TFLInsertArrivalData(collection: MongoCollection) extends DatabaseModifyInterface {


  override def insertDocument(line: Line) = {
    val newObj = MongoDBObject(line.geFieldValueList())

    /*
      BUS_ROUTE.productPrefix -> line.getField(BUS_ROUTE),
      BUS_STOP_CODE.productPrefix -> line.getField(BUS_STOP_CODE),
      BUS_REG.productPrefix -> line.getField(BUS_REG),
      BUS_DIRECTION_ID.productPrefix-> line.getField(BUS_DIRECTION_ID),
      BUS_ARRIVAL_TIME.productPrefix-> line.getField(BUS_ARRIVAL_TIME))
      */

    val writeResult = collection.insert(newObj)

  }


}
