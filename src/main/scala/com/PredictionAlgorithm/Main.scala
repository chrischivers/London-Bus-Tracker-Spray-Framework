package com.PredictionAlgorithm
import javax.swing.{SwingUtilities, JFrame}
import akka.actor.{Props, ActorSystem}
import com.PredictionAlgorithm.Processes.StartMessage
import com.PredictionAlgorithm.Processes.TFL.TFLProcessArrivalStream

/**
 * Created by chrischivers on 21/06/15.
 */
object Main extends App {



  implicit val actorSystem = ActorSystem()
  val asActor = actorSystem.actorOf(Props[TFLProcessArrivalStream], name = "TFLArrivalStream")
   asActor ! StartMessage


}
