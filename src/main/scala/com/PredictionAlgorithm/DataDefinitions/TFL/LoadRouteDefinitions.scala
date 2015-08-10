package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io.{PrintWriter, File}

import akka.actor.{Props, Actor}
import com.PredictionAlgorithm.ControlInterface.StreamProcessingControlInterface._
import com.PredictionAlgorithm.DataDefinitions.LoadResource
import com.PredictionAlgorithm.DataDefinitions.Tools.FetchPolyLines
import com.PredictionAlgorithm.Database.{ROUTE_DEFINITION_DOCUMENT, ROUTE_DEFINITIONS_COLLECTION, DatabaseCollections}
import com.PredictionAlgorithm.Database.TFL.{TFLInsertUpdateRouteDefinition, TFLGetRouteDefinitionDocument}


import scala.io.Source

object LoadRouteDefinitions extends LoadResource {

  var percentageComplete = 0
  private val collection = ROUTE_DEFINITIONS_COLLECTION

  //Rotue definition Map (RouteID Direction -> List of Point Sequence, Stop Code, FirstLast, PolyLine
  private var routeDefinitionMap: Map[(String, Int), List[(Int, String, Option[String], String)]] = Map()

  def getRouteDefinitionMap: Map[(String, Int), List[(Int, String, Option[String], String)]]  = {
    if (routeDefinitionMap.isEmpty) {
      retrieveFromDB
      routeDefinitionMap
    } else routeDefinitionMap
  }

  private def retrieveFromDB: Unit = {
    var tempMap:Map[(String, Int), List[(Int, String, Option[String], String)]]  = Map()

    val cursor = TFLGetRouteDefinitionDocument.fetchAll()
    for(doc <- cursor) {
      val routeID = doc.get(collection.ROUTE_ID).asInstanceOf[String]
      val direction = doc.get(collection.DIRECTION_ID).asInstanceOf[Int]
      val sequence = doc.get(collection.SEQUENCE).asInstanceOf[Int]
      val stop_code = doc.get(collection.STOP_CODE).asInstanceOf[String]
      val firstLast = doc.get(collection.FIRST_LAST).asInstanceOf[String]
      val polyLine = doc.get(collection.POLYLINE).asInstanceOf[String]
      val firstLastOption = if (firstLast == null) None else Some(firstLast)

      val listFromTempMap = tempMap.get(routeID,direction)
      if (listFromTempMap.isDefined) {
        val list = listFromTempMap.get
        val combinedList: List[(Int, String, Option[String], String)] = list ++ List((sequence,stop_code,firstLastOption, polyLine))
        tempMap += ((routeID,direction) -> combinedList.sortBy(_._1))
      } else {
        tempMap += ((routeID,direction) -> List((sequence,stop_code,firstLastOption, polyLine)))
      }
    }
    routeDefinitionMap = tempMap
    println("Number route definitions fetched from DB: " + routeDefinitionMap.size)
  }

  def updateFromWeb: Unit = {
    val streamActor = actorSystem.actorOf(Props[UpdateRouteDefinitionsFromWeb], name = "UpdateRouteDefinitionsFromWeb")
    streamActor ! "start"
  }


  class UpdateRouteDefinitionsFromWeb extends Actor {

    override def receive: Receive = {
      case "start" => updateFromWeb
    }


    def updateFromWeb: Unit = {

      var tempMap: Map[(String, Int), List[(Int, String, Option[String], String)]] = Map()


      println("Loading Route Definitions From Web...")

      // RouteName, WebCode
      var routeSet: Set[(String, String)] = Set()
      val routeListFile  = DEFAULT_ROUTE_LIST_FILE
      val numberLinesInFile = routeListFile.getLines().size
      println("Lines in file: " + numberLinesInFile)
      var numberLinesProcessed = 0


      routeListFile.getLines.drop(1).foreach((line) => {
        //drop first row and iterate through others
        try {
          val splitLine = line.split(",")
          val route_ID = splitLine(0)
          val route_Web_ID = splitLine(1)
          for (direction <- 1 to 2) {
            val stopListFromWeb = getStopList(route_Web_ID, direction)
              if (stopListFromWeb.isDefined) {
                stopListFromWeb.get.foreach {
                  case (stopCode, pointSeq, first_last) => {

                    val listFromTempMap = tempMap.get(route_ID, direction)
                    if (listFromTempMap.isDefined) {
                      val list = listFromTempMap.get
                      val combinedList = list ++ List((pointSeq, stopCode, first_last, ""))
                      tempMap += ((route_ID, direction) -> combinedList.sortBy(_._1))
                    } else {
                      tempMap += ((route_ID, direction) -> List((pointSeq, stopCode, first_last, "")))
                    }
                  }
                }
              }
          }
          numberLinesProcessed += 1
          percentageComplete = ((numberLinesProcessed.toDouble / numberLinesInFile.toDouble) * 100).toInt
        }
        catch {
          case e: ArrayIndexOutOfBoundsException => throw new Exception("Error reading route list file. Error on line: " + line)
        }
      })

      percentageComplete = 100
      println("Route Definitions from web loaded")
      routeDefinitionMap = tempMap
      persistToDB
    }


    private def getStopList(webRouteID: String, direction: Int): Option[List[(String, Int, Option[String])]] = {

      var stopCodeSequenceList: List[(String, Int, Option[String])] = List()

      var tflURL: String = if (direction == 1) {
        "http://m.countdown.tfl.gov.uk/showJourneyPattern/" + webRouteID + "/Outbound"
      } else if (direction == 2) {
        "http://m.countdown.tfl.gov.uk/showJourneyPattern/" + webRouteID + "/Back"
      } else {
        throw new IllegalStateException("Invalid direction ID")
      }

      val s = Source.fromURL(tflURL)
      var pointSequence = 1
      var skipThisRoute = false //TODO If webpage is in irregular format, we skip this. Needs to be logged. And dealt with if time.
      var viaEncountered,toEncountered = false

      s.getLines.foreach((line) => {
        // For some routes there are mutliple pattern segments (e.g. route 134). This discards the first one if a second one follows (only second is kept)
        if (line.contains("route segment icon") && line.contains(";From")) {
          pointSequence = 1
          stopCodeSequenceList = List()
        }

        if (line.contains("route segment icon") && line.contains(";Via")) {
          //if (viaEncountered) skipNext = true
          //viaEncountered = true
          skipThisRoute = true
        }

        if (line.contains("route segment icon") && line.contains(";To")) {
          //if (toEncountered) skipNext = true
         // toEncountered = true
          skipThisRoute = true
        }

        if (line.contains("<dd><a href=")) {
            val startChar: Int = line.indexOf("searchTerm=") + 11
            val endChar: Int = line.indexOf("+")
            val stopCode = line.substring(startChar, endChar)
            val first_last: Option[String] = {
              if (pointSequence == 1) Some("FIRST") else None
            }

            stopCodeSequenceList = stopCodeSequenceList :+ ((stopCode, pointSequence, first_last))
            pointSequence += 1
        }
      })

        // Set LAST on last option
        val lastone: List[(String, Int, Option[String])] = stopCodeSequenceList.takeRight(1).map { case (x, y, z) => (x, y, Some("LAST")) }
        stopCodeSequenceList = stopCodeSequenceList.dropRight(1) ::: lastone
      if (!skipThisRoute) Some(stopCodeSequenceList) else {
      //  println("Skipped: " + webRouteID + ", " + direction)
        None
      }
    }


    private def persistToDB: Unit = {

      routeDefinitionMap.foreach {
        case ((routeID, direction), list) => {
          list.foreach {
            case (sequence, stopCode, firstLast, polyLine) => {
              val newDoc = new ROUTE_DEFINITION_DOCUMENT(routeID, direction, sequence, stopCode, firstLast)
              TFLInsertUpdateRouteDefinition.insertDocument(newDoc)
            }
          }

        }
      }
      println("Route Definitons loaded from web and persisted to DB")

    }


  }

}
