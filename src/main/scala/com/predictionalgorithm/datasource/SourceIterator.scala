package com.predictionalgorithm.datasource


class SourceIterator(dataStream: DataStream) extends Iterable[String] {

  override def iterator: Iterator[String] = {
    val x= dataStream.getStream.iterator
    disregardNLines(x)
    x
  }
   //disregards the first N lines from the Stream as specified in the variables


  private def disregardNLines(iterator: Iterator[String]) = {
    for (a <- 1 to dataStream.getNumberLinesToDisregard) {
      iterator.next()
    }
  }


}
