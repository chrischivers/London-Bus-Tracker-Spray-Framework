package com.predictionalgorithm.datasource.tfl

import com.predictionalgorithm.datasource.SourceLineFormatter
import grizzled.slf4j.Logger


object TFLSourceLineFormatterImpl extends SourceLineFormatter{
  val logger = Logger[this.type]

  override def apply(sourceLineString: String): TFLSourceLineImpl = {
    val x = splitLine(sourceLineString)
    checkArrayCorrectLength(x)
    new TFLSourceLineImpl(x(0), x(1), x(2).toInt, x(3), x(4).toLong)
  }


  def splitLine(line: String) = line
    .substring(1,line.length-1) // remove leading and trailing square brackets,
    .replaceAll("\"","") //take out double quotations
    .split(",") // split at commas
    .tail // discards the first element (always '1')

  def checkArrayCorrectLength(array: Array[String]) = {
    if (array.length != TFLDataSourceImpl.fieldVector.length) {
      logger.debug("Source array has incorrect number of elements. Or invalid web page retrieved \n " + array)
      throw new IllegalArgumentException("Source array has incorrect number of elements. Or invalid web page retrieved \n " + array)
    }
  }
}
