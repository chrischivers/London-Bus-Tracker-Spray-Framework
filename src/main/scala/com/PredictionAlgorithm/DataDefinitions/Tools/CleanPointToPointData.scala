package com.PredictionAlgorithm.DataDefinitions.Tools

import akka.actor.Actor
import akka.actor.Actor.Receive
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Database.TFL.TFLGetPointToPointDocument

/**
 * Created by chrischivers on 02/08/15.
 */
object CleanPointToPointData extends App{

}

class CleanPointToPointData extends Actor {

  val collection = POINT_TO_POINT_COLLECTION
  val routeDefinitions = TFLDefinitions.RouteDefinitionMap

  override def receive: Receive = {
    case "start" => startClean
  }

  def startClean = {
    val cursor = TFLGetPointToPointDocument.fetchAll()
    for (doc <- cursor) {
      val routeID = doc.get(collection.ROUTE_ID).asInstanceOf[String]
      val direction = doc.get(collection.DIRECTION_ID).asInstanceOf[Int]
      val fromStop = doc.get(collection.FROM_POINT_ID).asInstanceOf[String]
      val toStop = doc.get(collection.TO_POINT_ID).asInstanceOf[String]

     val fromPointSeqFromDef = routeDefinitions(routeID,direction).filter(x => x._2 == fromStop).head._1
      val toPointSeqFromDef = routeDefinitions(routeID,direction).filter(x => x._2 == toStop).last._1

      if(fromPointSeqFromDef + 1 != toPointSeqFromDef) {
          println("Mismatch in PointToPoint")
      }


    }
  }
}