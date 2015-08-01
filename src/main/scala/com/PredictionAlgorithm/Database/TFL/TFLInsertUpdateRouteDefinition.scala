package com.PredictionAlgorithm.Database.TFL

import akka.actor.{Actor, Props, ActorRef}
import com.PredictionAlgorithm.Database._

import com.mongodb.casbah.commons.{MongoDBObject, Imports}


object TFLInsertUpdateRouteDefinitionDocument extends DatabaseModifyInterface{

  @volatile var numberDBUpdatesRequested = 0
  @volatile var numberPolyLinesInserted = 0
  @volatile var numberDBInsertsRequested = 0

  override val dbModifyActor: ActorRef = actorSystem.actorOf(Props[TFLInsertUpdateRouteDefinitionDocument], name = "TFLInsertRouteDefinitionActor")

  override protected val collection: DatabaseCollections = ROUTE_DEFINITIONS_COLLECTION

  def updateDocumentWithPolyLine (doc: DatabaseDocuments, polyLine:String): Unit = {
    numberPolyLinesInserted += 1
    dbModifyActor ! (doc, polyLine)
  }
}

class TFLInsertUpdateRouteDefinitionDocument extends Actor {

  val collection = ROUTE_DEFINITIONS_COLLECTION

  override def receive: Receive = {
    case doc: ROUTE_DEFINITION_DOCUMENT => insertToDB(doc)
    case (doc: ROUTE_DEFINITION_DOCUMENT, polyline:String) => updateDbWithPolyline(doc,polyline)
    case _ => throw new IllegalStateException("TFL Route Definition Actor received unknown message")
  }


  private def insertToDB(doc: ROUTE_DEFINITION_DOCUMENT) = {

    val newObj = MongoDBObject(
      collection.ROUTE_ID -> doc.route_ID,
      collection.DIRECTION_ID -> doc.direction_ID,
      collection.SEQUENCE -> doc.sequence,
      collection.STOP_CODE-> doc.stop_Code,
      collection.FIRST_LAST-> doc.first_Last)


    val cursor = TFLGetRouteDefinitionDocument.executeQuery(newObj)
    if(cursor.length == 1) {
      val dbObject = cursor.next()
      if (dbObject.equals(newObj)) {
        println("route def same")
      } else {
        val query = MongoDBObject(
          collection.ROUTE_ID -> doc.route_ID,
          collection.DIRECTION_ID -> doc.direction_ID,
          collection.SEQUENCE -> doc.sequence)
        TFLInsertUpdateRouteDefinitionDocument.dBCollection.update(query, newObj, upsert = true)
        TFLInsertUpdateRouteDefinitionDocument.numberDBUpdatesRequested += 1
      }
    } else {
      TFLInsertUpdateRouteDefinitionDocument.dBCollection.insert(newObj)
      TFLInsertUpdateRouteDefinitionDocument.numberDBInsertsRequested+= 1
    }

  }

  private def updateDbWithPolyline(doc: ROUTE_DEFINITION_DOCUMENT, polyLineEncoded:String) = {
    val query = MongoDBObject(
      collection.ROUTE_ID -> doc.route_ID,
      collection.DIRECTION_ID -> doc.direction_ID,
      collection.SEQUENCE -> doc.sequence,
      collection.STOP_CODE-> doc.stop_Code,
      collection.FIRST_LAST -> doc.first_Last)

    val newObj = MongoDBObject(
      collection.ROUTE_ID -> doc.route_ID,
      collection.DIRECTION_ID -> doc.direction_ID,
      collection.SEQUENCE -> doc.sequence,
      collection.STOP_CODE-> doc.stop_Code,
      collection.FIRST_LAST -> doc.first_Last,
      collection.POLYLINE -> polyLineEncoded
    )

    TFLInsertUpdateRouteDefinitionDocument.dBCollection.update(query, newObj, upsert = true)
  }

}