package com.PredictionAlgorithm.Processes.TFL


import java.net.UnknownHostException

import com.PredictionAlgorithm.DataSource.TFL.{TFLSourceLineFormatter, TFLDataSource, TFLSourceLine}
import com.PredictionAlgorithm.DataSource._
import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Processes.{StartMessage, IterateOverArrivalStreamInterface}

import scala.util.{Failure, Success, Try}


class TFLIterateOverArrivalStream extends IterateOverArrivalStreamInterface {

  //val logger = Logger(LoggerFactory.getLogger("DataSourceLog"))


  override def getSourceIterator =
    Try(new SourceIterator(new HttpDataStream(TFLDataSource))) match {
      case Success(src) => src.iterator
      case Failure(fail) => throw new IllegalStateException("Cannot get Source Iterator")
    }


  override def startIterating(src: Iterator[String]) = {

    println("TFL Arrival Stream Starting Iterating")
      while (src.hasNext) {
        val line = TFLSourceLineFormatter(src.next())
        TFLProcessSourceLines(line)
        numberProccessed += 1
        if (numberProccessed % 10 == 0) println(numberProccessed)
      }
      println("out of while block")
  }

  override def start = {
    println("Here")
    try {
      startIterating(getSourceIterator)
    } catch {
        case e @ (_ : IllegalStateException | _ : UnknownHostException) => {
                  println(e.getMessage)
                  println("Exception thrown in Arrival Stream. Sleeping, before retrying...")
                  Thread.sleep(TFLProcessVariables.TIMEOUT)
                  start
        }
        case e:Throwable => println("unknown exception thrown in TFL Process Arrival Stream")
                  e.printStackTrace()

    }

  }

}
