package com.PredictionAlgorithm.DataSource

import com.PredictionAlgorithm.DataSource.TFL.TFLDataSourceVariables

class SourceIterator(dataStream: Iterable[String]) extends Iterable[String] {

  override def iterator: Iterator[String] = {
    val x= dataStream.iterator
    disregardNLines(x)
    x
  }
   //disregards the first N lines from the Stream as specified in the variables


  private def disregardNLines(iterator: Iterator[String]) = {
    for (a <- 1 to TFLDataSourceVariables.NUMBER_LINES_TO_DISREGARD) {
      iterator.next()
    }
  }


}
