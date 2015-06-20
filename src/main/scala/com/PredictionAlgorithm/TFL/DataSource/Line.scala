package com.PredictionAlgorithm.TFL.DataSource

/**
 * Created by chrischivers on 19/06/15.
 */
class Line (line: String) {

  val splitLineArray = splitLine(line)
  checkLineArrayLength(splitLineArray)


  def getField(name: FieldName) = name match {
    case STOP_CODE => splitLineArray(0)
    case BUS_ROUTE => splitLineArray(1)
    case DIRECTION => splitLineArray(2)
    case REG_NUMBER => splitLineArray(3)
    case ARRIVAL_TIME => splitLineArray(4)
    case _ => throw new IllegalArgumentException("Invalid Array Field Name")
  }

  def splitLine(line: String) = line
      .substring(1,line.length-2) // remove leading and trailing square brackets,
      .replaceAll("\"","") //take out double quotations
      .split(",") // split at commas
      .tail // discards the first element (always '1')

  def checkLineArrayLength(splitLineArray: Array[String]) = {
    if (splitLineArray.length < 5) {
      throw new IllegalArgumentException("Source array has incorrect number of elements")
    }
  }

}

