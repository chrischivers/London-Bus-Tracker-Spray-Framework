package com.PredictionAlgorithm.Processes.TFL


import java.net.UnknownHostException

import com.PredictionAlgorithm.DataSource.TFL.TflLine
import com.PredictionAlgorithm.DataSource._
import com.PredictionAlgorithm.Database.ARRIVAL_LOG_COLLECTION
import com.PredictionAlgorithm.Database.TFL.{TFLInsertArrivalData, TFLMongoDBConnection}
import com.PredictionAlgorithm.Processes.{StartMessage, ProcessArrivalStreamInterface}
import com.mongodb.casbah.MongoCollection

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}


class TFLProcessArrivalStream extends ProcessArrivalStreamInterface {

  override var numberProccessed: Int = 0

  //val logger = Logger(LoggerFactory.getLogger("DataSourceLog"))
  override lazy val dbCollection = getDBCollection


  override def getDBCollection =
    Try(new TFLMongoDBConnection().getCollection(ARRIVAL_LOG_COLLECTION)) match {
      case Success(collection) => collection
      case Failure(fail) => throw new IllegalStateException("Cannot get DB Collection")

    }

  override def getSourceIterator =
    (Try(HttpDataSource.getDataStream).flatMap(ds =>
      Try(new SourceIterator(ds)))) match {
      case Success(src) => src.iterator
      case Failure(fail) => throw new IllegalStateException("Cannot get Source Iterator")
    }


  override def startIterating(src: Iterator[String]) = {
    println("TFL Arrival Stream Starting Iterating")
      while (src.hasNext) {
        val line = new TflLine(src.next())
        TFLInsertArrivalData(dbCollection).insertDocument(line)
        numberProccessed += 1
        if (numberProccessed % 1000 == 0) println(numberProccessed)
      }
      println("out of while block")
  }

  override def start = {
    try {
      startIterating(getSourceIterator)
    } catch {
        case e @ (_ : IllegalStateException | _ : UnknownHostException) => {
                  println(e.getMessage)
                  println("Exception thrown in Arrival Stream. Sleeping, before retrying...")
                  Thread.sleep(ProcessVariables.TIMEOUT)
                  start
        }
        case e => println("unknown exception thrown in TFL Process Arrival Stream")
                  e.printStackTrace()

    }

  }

}
