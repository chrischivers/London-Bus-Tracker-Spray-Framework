package com.PredictionAlgorithm.Database.TFL

import akka.actor.{Actor, Props, ActorRef}
import com.PredictionAlgorithm.Database._
import com.mongodb.casbah.commons.{Imports, MongoDBObject}


object TFLInsertStopDefinition extends DatabaseInsertInterface{

  @volatile var numberDBUpdatesRequested = 0
  @volatile var numberDBInsertsRequested = 0

  override val dbInsertActor: ActorRef = actorSystem.actorOf(Props[TFLInsertStopDefinition], name = "TFLInsertStopDefinitionActor")

  override protected val collection: DatabaseCollections = STOP_DEFINITIONS_COLLECTION

}

class TFLInsertStopDefinition extends Actor {

  val collection = STOP_DEFINITIONS_COLLECTION

  override def receive: Receive = {
    case doc1: STOP_DEFINITION_DOCUMENT => insertToDB(doc1)
    case _ => throw new IllegalStateException("TFL Stop Definition Actor received unknown message")
  }


  private def insertToDB(doc: STOP_DEFINITION_DOCUMENT) = {

    val newObj = MongoDBObject(
      collection.STOP_CODE -> doc.stopCode,
      collection.STOP_NAME -> doc.stopName,
      collection.STOP_TYPE -> doc.stopType,
      collection.TOWARDS -> doc.towards,
      collection.BEARING -> doc.bearing,
      collection.INDICATOR -> doc.indicator,
      collection.STATE -> doc.state,
      collection.LAT -> doc.lat,
      collection.LNG -> doc.lng)


    val cursor = TFLGetStopDefinitionDocument.executeQuery(newObj)
    if(cursor.length == 1) {
      val dbObject = cursor.next()
      if (dbObject.equals(newObj)) {
        println("stop def same")
      } else {
        val query = MongoDBObject(
          collection.STOP_CODE -> doc.stopCode)
        TFLInsertStopDefinition.dBCollection.update(query, newObj, upsert = true)
        TFLInsertStopDefinition.numberDBUpdatesRequested += 1
      }
    } else {
      TFLInsertStopDefinition.dBCollection.insert(newObj)
      TFLInsertStopDefinition.numberDBInsertsRequested+= 1
    }

  }

}