package com.predictionalgorithm.datasource


class SourceIterator(dataStream: DataStream) extends Iterable[String] {

  override def iterator: Iterator[String] = {
    val iterator = dataStream.getStream.iterator
    disregardNLines(iterator)

  }
   //disregards the first N lines from the Stream as specified in the variables


  private def disregardNLines(iterator: Iterator[String]) = {
    val x= dataStream.getStream
    x.drop(dataStream.getNumberLinesToDisregard).iterator
  }


}
