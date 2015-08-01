package com.PredictionAlgorithm.DataDefinitions.Tools

import akka.actor.{Actor, Props}
import com.PredictionAlgorithm.ControlInterface.StreamProcessingControlInterface._
import com.PredictionAlgorithm.DataDefinitions.LoadResource
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Database.TFL.{TFLGetRouteDefinitionDocument, TFLInsertUpdateRouteDefinitionDocument}
import com.PredictionAlgorithm.Database.{ROUTE_DEFINITIONS_COLLECTION, ROUTE_DEFINITION_DOCUMENT}
import com.mongodb.casbah.Imports

import scala.io.Source

object AddPolyLines extends LoadResource {

  var numberLinesProcessed = 0
  var numberPolyLinesUpdated = 0

  def add = {
    val streamActor = actorSystem.actorOf(Props[AddPolyLinesActor], name = "AddPolyLinesActor")
    streamActor ! "start"
  }


  class AddPolyLinesActor extends Actor {


    override def receive: Receive = {
      case "start" => getAllRoutes
    }

    val collection = ROUTE_DEFINITIONS_COLLECTION
    var polySet: Set[(String, String, String)] = Set()
    var APICurrentIndex = 0

    def getAllRoutes = {
      val cursor = TFLGetRouteDefinitionDocument.fetchAllOrdered()
      var docLastRead: Option[Imports.DBObject] = None
      var tempCOunt = 0;
      for (doc <- cursor) {
        if (getAPIKeys.isDefined) {
          if (doc.get(collection.FIRST_LAST).asInstanceOf[String] == "FIRST") {
            docLastRead = Some(doc)
          }
          else {
            if (docLastRead.getOrElse(throw new IllegalStateException("Error. Last read document is None: " + doc)).get(collection.POLYLINE) == null) {
              //Only do if no polyline recorded
              val thisDoc = doc.get(collection.STOP_CODE).asInstanceOf[String]

              val thisStopCodeLat = TFLDefinitions.StopDefinitions(thisDoc).latitude
              val thisStopCodeLng = TFLDefinitions.StopDefinitions(thisDoc).longitude

              val lastStopCodeLat = TFLDefinitions.StopDefinitions(docLastRead.get.get(collection.STOP_CODE).asInstanceOf[String]).latitude
              val lastStopCodeLng = TFLDefinitions.StopDefinitions(docLastRead.get.get(collection.STOP_CODE).asInstanceOf[String]).longitude


              val url = "https://maps.googleapis.com/maps/api/directions/xml?origin=" + lastStopCodeLat + "," + lastStopCodeLng + "&destination=" + thisStopCodeLat + "," + thisStopCodeLng + "&key=" + getAPIKeys + "&mode=driving"
              val s = Source.fromURL(url).getLines()
              for (line <- s) {
                if (line.contains("OVER_QUERY_LIMIT")) {
                  APICurrentIndex += 1
                }
                else if (line.contains("<overview_polyline>")) {
                  val pointsLine = s.next()
                  val polyLine = pointsLine.substring(11, pointsLine.length - 9)
                  addPolyLinetoDB(docLastRead.get, polyLine) //Updates previous with polyline route to next
                  numberPolyLinesUpdated += 1
                }
              }
            }
            if (doc.get(collection.FIRST_LAST).asInstanceOf[String] != "LAST") docLastRead = Some(doc)
            else docLastRead = None

          }
          numberLinesProcessed += 1
        } else {
          println("No more API keys available")
        }
      }
      println("Finished PolyLineProcessing")
    }

    private def addPolyLinetoDB(doc: Imports.DBObject, polyLine: String) = {
      val routeID = doc.get(collection.ROUTE_ID).asInstanceOf[String]
      val direction = doc.get(collection.DIRECTION_ID).asInstanceOf[Int]
      val sequence = doc.get(collection.SEQUENCE).asInstanceOf[Int]
      val stopCode = doc.get(collection.STOP_CODE).asInstanceOf[String]
      val firstLast = if (doc.get(collection.FIRST_LAST) == null) None else Some(doc.get(collection.FIRST_LAST).asInstanceOf[String])
      val routeDocToUpdate = new ROUTE_DEFINITION_DOCUMENT(routeID, direction, sequence, stopCode, firstLast)
      TFLInsertUpdateRouteDefinitionDocument.updateDocumentWithPolyLine(routeDocToUpdate, polyLine)
    }

    private def getAPIKeys():Option[String] = {
      val APIKeys = List(
        "AIzaSyD-9dP1VD-Ok9-oY1aXhSZZCYR5CRo-Jus",
        "AIzaSyDSEq-FMJhzFbQNIgK1JNQZuaLcPFV3oxw",
      "AIzaSyDcuDPhqrEoVPoLxoeeLpWwx07fYjFqSeM",
      "AIzaSyCHLODVvW1s20QhS_zyKEAYnlbvsC6Gu9w",
      "AIzaSyAj6_kBtnllfulkTG0aih6onOnf9Qm5cX0",
      "AIzaSyAcWJNih_q90XV4ufyoNWpzzEsMP1PoLz0")
      if (APICurrentIndex < APIKeys.length) Some(APIKeys(APICurrentIndex)) else None
    }

  }

}

//