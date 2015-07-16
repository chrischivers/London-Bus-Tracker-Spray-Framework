package com.PredictionAlgorithm.DataSource.TFL

import com.PredictionAlgorithm.DataSource.{SourceLine, SourceLineProcessor}

/**
 * Created by chrischivers on 12/07/15.
 */
object TFLSourceLineFormatter extends SourceLineProcessor{
  override def apply(sourceLineString: String): TFLSourceLine = {
    val x = splitLine(sourceLineString)
    checkArrayCorrectLength(x)
    new TFLSourceLine(x(0), x(1), x(2).toInt, x(3), x(4).toLong)
  }


  def splitLine(line: String) = line
    .substring(1,line.length-2) // remove leading and trailing square brackets,
    .replaceAll("\"","") //take out double quotations
    .split(",") // split at commas
    .tail // discards the first element (always '1')

  def checkArrayCorrectLength(array: Array[String]) = {
    if (array.length != TFLDataSource.fieldVector.length) {
      throw new IllegalArgumentException("Source array has incorrect number of elements. Or invalid web page retrieved \n " + array)
    }
  }
}
