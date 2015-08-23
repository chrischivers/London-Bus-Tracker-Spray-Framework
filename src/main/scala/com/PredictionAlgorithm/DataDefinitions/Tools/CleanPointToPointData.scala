package com.PredictionAlgorithm.DataDefinitions.Tools

import akka.actor.{PoisonPill, Props, Actor}
import com.PredictionAlgorithm.ControlInterface.StreamProcessingControlInterface._
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Database.TFL.{TFLDeletePointToPointDuration, TFLGetPointToPointDocument}
import com.mongodb.casbah.Imports
import org.bson.types.ObjectId

object CleanPointToPointData {

  private val streamActor = actorSystem.actorOf(Props[CleanPointToPointData], name = "CleanPointToPointDataActor")
  @volatile var numberDocumentsRead = 0
  @volatile var numberDocumentsDeleted = 0

  def start() = {
    streamActor ! "start"
  }

  def stop() = {
    streamActor ! PoisonPill
  }

}

class CleanPointToPointData extends Actor {

  val collection = POINT_TO_POINT_COLLECTION
  val routeDefinitions = TFLDefinitions.RouteDefinitionMap
  val cursor = TFLGetPointToPointDocument.fetchAll()

  override def receive: Receive = {
    case "start" => self ! cursor.next()
    case doc:Imports.DBObject => processNext(doc)
  }


  def processNext(doc:Imports.DBObject): Unit = {
    CleanPointToPointData.numberDocumentsRead += 1
    val routeID = doc.get(collection.ROUTE_ID).asInstanceOf[String]
    val direction = doc.get(collection.DIRECTION_ID).asInstanceOf[Int]
    val fromStop = doc.get(collection.FROM_POINT_ID).asInstanceOf[String]
    val toStop = doc.get(collection.TO_POINT_ID).asInstanceOf[String]
   val id = doc.get("_id").asInstanceOf[ObjectId]

    if (canDelete(routeID,direction,fromStop,toStop))  {
      CleanPointToPointData.numberDocumentsDeleted += 1
      println("Can delete the following: Route: " + routeID + ". Direction: " + direction + ". From Stop: " + fromStop + ". To Stop: " + toStop)
      TFLDeletePointToPointDuration.deleteDocument(id)
    }

    if (cursor.hasNext) self ! cursor.next()
  }

  def canDelete(routeID:String, direction:Int, fromStop:String, toStop:String): Boolean = {

    val fromStopReference = routeDefinitions.get(routeID,direction).getOrElse(return true).filter(x => x._2 == fromStop)
    val toStopReference = routeDefinitions.get(routeID,direction).getOrElse(return true).filter(x => x._2 == toStop)
    if (fromStopReference.isEmpty || toStopReference.isEmpty) return true

    val fromPointSeqFromDef = fromStopReference.head._1
    val toPointSeqFromDef = toStopReference.last._1
    if(fromPointSeqFromDef + 1 != toPointSeqFromDef) return true

    false
  }
}