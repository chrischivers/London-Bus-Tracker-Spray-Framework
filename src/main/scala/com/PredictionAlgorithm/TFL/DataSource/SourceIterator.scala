package com.PredictionAlgorithm.TFL.DataSource

class SourceIterator(dataStream: Stream[String]) {

  val source = dataStream.iterator
  disregardNLines //disregards the first N lines from the Stream as specified in the variables


  def hasNext: Boolean = source.hasNext

  def next(): String = source.next

  private def disregardNLines = {
    for (a <- 1 to DataSourceVariables.NUMBER_LINES_TO_DISREGARD) {
      source.next()
    }
  }


}
