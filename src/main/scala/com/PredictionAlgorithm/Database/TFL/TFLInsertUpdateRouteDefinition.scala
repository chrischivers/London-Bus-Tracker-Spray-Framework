package com.PredictionAlgorithm.Database.TFL

import akka.actor.{Actor, Props, ActorRef}
import com.PredictionAlgorithm.Database._

import com.mongodb.casbah.Imports._

import com.mongodb.casbah.commons.MongoDBObject


object TFLInsertUpdateRouteDefinition extends DatabaseInsertInterface{

  @volatile var numberDBUpdatesRequested = 0
  @volatile var numberDBInsertsRequested = 0
  @volatile var numberPolyLinesInserted = 0

  override val dbInsertActor: ActorRef = actorSystem.actorOf(Props[TFLInsertUpdateRouteDefinition], name = "TFLInsertRouteDefinitionActor")

  override protected val collection: DatabaseCollections = ROUTE_DEFINITIONS_COLLECTION

  def updateDocumentWithPolyLine (doc: DatabaseDocuments, polyLine:String): Unit = {
    numberPolyLinesInserted += 1
    dbInsertActor ! (doc, polyLine)
  }
}

class TFLInsertUpdateRouteDefinition extends Actor {

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
      collection.POINT_ID -> doc.stop_Code,
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
        TFLInsertUpdateRouteDefinition.dBCollection.update(query, newObj, upsert = true)
        TFLInsertUpdateRouteDefinition.numberDBUpdatesRequested += 1
      }
    } else {
      TFLInsertUpdateRouteDefinition.dBCollection.insert(newObj)
      TFLInsertUpdateRouteDefinition.numberDBInsertsRequested+= 1
    }

  }

  private def updateDbWithPolyline(doc: ROUTE_DEFINITION_DOCUMENT, polyLineEncoded:String) = {
    val query = MongoDBObject(
      collection.ROUTE_ID -> doc.route_ID,
      collection.DIRECTION_ID -> doc.direction_ID,
      collection.SEQUENCE -> doc.sequence,
      collection.POINT_ID-> doc.stop_Code,
      collection.FIRST_LAST -> doc.first_Last)

    val update = $set(collection.POLYLINE -> polyLineEncoded)

    TFLInsertUpdateRouteDefinition.dBCollection.update(query, update, upsert = true)
  }

}