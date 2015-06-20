package com.PredictionAlgorithm.TFL.DataSource

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}


object FetchTest extends App {

  val logger = Logger(LoggerFactory.getLogger("DataSourceLog"))

  setUpSourceIterator match {
    case Success(iterator) => startIterating(iterator)
    case Failure(_) => throw new IllegalStateException("Cannot set up the Data Source Iterator")
  }

  def setUpSourceIterator =
    Try(DataSource.getDataStream).flatMap(ds =>
      Try(new SourceIterator(ds)))


  def startIterating(src: SourceIterator) = {
    while (src.hasNext) {
      val line = new Line(src.next())

      print(line.getField(BUS_ROUTE))
      print(line.getField(STOP_CODE))
      print(line.getField(REG_NUMBER))
      println(line.getField(ARRIVAL_TIME))
    }

  }
}
