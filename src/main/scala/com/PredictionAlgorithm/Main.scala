package com.PredictionAlgorithm

import com.PredictionAlgorithm.DataSource.TFL.TflLine
import com.PredictionAlgorithm.DataSource._
import com.PredictionAlgorithm.Database.ARRIVAL_LOG_COLLECTION
import com.PredictionAlgorithm.Database.TFL.{TFLMongoDBConnection, TFLInsertArrivalData}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}


object Main extends App {

  val logger = Logger(LoggerFactory.getLogger("DataSourceLog"))

  val dbCollection = new TFLMongoDBConnection().getCollection(ARRIVAL_LOG_COLLECTION)

  setUpSourceIterator match {
    case Success(src) => startIterating(src.iterator)
    case Failure(_) => throw new IllegalStateException("Cannot set up the Data Source Iterator")
  }

  def setUpSourceIterator =
    Try(HttpDataSource.getDataStream).flatMap(ds =>
      Try(new SourceIterator(ds)))


  def startIterating(src: Iterator[String]) = {
    while (src.hasNext) {
      val line = new TflLine(src.next())
      TFLInsertArrivalData(dbCollection).insertDocument(line)
    }

  }

}
