package com.PredictionAlgorithm

import java.io.File
import javax.swing.{SwingUtilities, JFrame}
import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLRouteDefinitions
import com.PredictionAlgorithm.Database.TFL.TFLMongoDBConnection
import com.PredictionAlgorithm.Processes.StartMessage
import com.PredictionAlgorithm.Processes.TFL.TFLIterateOverArrivalStream

/**
 * Created by chrischivers on 21/06/15.
 */
object Main extends App {


  implicit val actorSystem = ActorSystem()
  val routeDefs = TFLRouteDefinitions.getTFLSequenceMap

  val streamActor = actorSystem.actorOf(Props[TFLIterateOverArrivalStream], name = "TFLArrivalStream")
  streamActor ! StartMessage


}
