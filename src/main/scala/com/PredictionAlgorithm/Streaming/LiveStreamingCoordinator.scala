package com.PredictionAlgorithm.Streaming

import java.util.concurrent.{LinkedBlockingQueue, BlockingQueue}

import akka.actor.Status.{Failure, Success}
import akka.actor._
import akka.actor.Actor.Receive
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.DataSource.TFL.TFLSourceLine

import scala.collection.mutable
import scala.concurrent.duration._

//case class LiveStreamResult(nextPointSeq: Int,nextStopCode: String, nextStopName:String, nextStopLate:Double, nextStopLng: Double, timeTilNextStop:Int)


object LiveStreamingCoordinator {

  implicit val actorSystem = ActorSystem("live_streaming")
  implicit val timeout = 1000

  val x = new FIFOStream
  var liveActors = Map[String, ActorRef]()

  def setObjectPosition(liveSourceLine: TFLSourceLine): Unit = {
    val vehicleReg = liveSourceLine.vehicle_Reg
      if (liveActors.contains(vehicleReg)) {
          liveActors(vehicleReg) ! liveSourceLine
      } else {

        val newActor:ActorRef = actorSystem.actorOf(Props(new VehicleActor(vehicleReg, liveSourceLine.route_ID, liveSourceLine.direction_ID)), vehicleReg)
        liveActors = liveActors + (vehicleReg -> newActor)
        newActor ! liveSourceLine //Start it off
    }
  }

  def getNumberLiveActors = liveActors.size

  def getStream = x.toStream



}

// Implementation adapted from Stack Overflow article:
//http://stackoverflow.com/questions/7553270/is-there-a-fifo-stream-in-scala
class FIFOStream {
  private val queue = new LinkedBlockingQueue[Option[(String, Double, Double)]]

  def toStream: Stream[(String, Double, Double)] = queue take match {
    case Some((a:String,b:Double, c:Double)) => Stream cons ( (a,b,c), toStream )
    case None => Stream empty
  }
  def close() = queue add None
  def enqueue(as:(String, Double, Double)) = queue add Some(as)
}

class VehicleActor(vehicle_ID: String, routeID: String, directionID: Int) extends Actor{

  import context.dispatcher

  var currentPosition:(Double,Double) = (0.0, 0.0)
  var predictedPositionQueue: mutable.Queue[(Long, Double, Double)] = mutable.Queue()
  val definitions = TFLDefinitions.StopDefinitions

  override def receive: Receive = {
    case sourceLine:TFLSourceLine => processNewLine(sourceLine)
    case "next" =>
      if (predictedPositionQueue.nonEmpty) {
        val head = predictedPositionQueue.dequeue
        in(Duration(System.currentTimeMillis() - head._1, MILLISECONDS)) {
          val lat = head._2
          val lng = head._3
          currentPosition = (lat, lng)
          LiveStreamingCoordinator.x.enqueue(vehicle_ID, lat, lng)
          self ! "next"
          //TODO KILL if last

        }
      }

  }

  def processNewLine(sourceLine:TFLSourceLine) = {
    val lat = definitions(sourceLine.stop_Code).latitude
    val lng = definitions(sourceLine.stop_Code).longitude
   // predictedPositionQueue = predictedPositionQueue.filter(_._1 < sourceLine.arrival_TimeStamp) //remove anythign in the queue ahead of the stream input (redundant)
    addToPredictedPositionQueue(sourceLine.arrival_TimeStamp,lat, lng) //TODO this is where the subpoints are fetched... AND where more paramaters may need to be passed
    self ! "next"
  }

  def addToPredictedPositionQueue(timestamp:Long, latitude: Double, longitude: Double) = {
    predictedPositionQueue.enqueue((timestamp,latitude,longitude))
  }

  def in[U](duration: FiniteDuration)(body: => U): Unit =
    LiveStreamingCoordinator.actorSystem.scheduler.scheduleOnce(duration)(body)
}
