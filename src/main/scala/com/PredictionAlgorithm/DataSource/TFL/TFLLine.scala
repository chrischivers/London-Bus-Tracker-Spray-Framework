package com.PredictionAlgorithm.DataSource.TFL

import com.PredictionAlgorithm.DataSource._

/**
 * Created by chrischivers on 19/06/15.
 */
class TflLine (line: String) extends Line {

  val splitLineArray = splitLine(line)
  checkArrayCorrectLength()


  def splitLine(line: String) = line
      .substring(1,line.length-2) // remove leading and trailing square brackets,
      .replaceAll("\"","") //take out double quotations
      .split(",") // split at commas
      .tail // discards the first element (always '1')

  def checkArrayCorrectLength() = {
    if (splitLineArray.length != TflLine.fieldNames.length) {
      throw new IllegalArgumentException("Source array has incorrect number of elements")
    }
  }

  override def geFieldValueList(): List[(String, String)] = {
      TflLine.fieldNames
       .zip(splitLineArray)//zips array fields with their key values
    .toList //Convert to list
  }
}

object TflLine {
  val fieldNames = TFLDataSourceVariables.FIELD_ORDER
    .map(x => x.productPrefix) // Gets the name of the field
}
