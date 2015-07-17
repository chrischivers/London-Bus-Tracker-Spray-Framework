package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io.File

import com.PredictionAlgorithm.DataDefinitions.LoadResource

import scala.io.Source

/**
 * Created by chrischivers on 17/07/15.
 */
object LoadStopIgnoreListFromFile extends LoadResource{


  private val stopIgnoreListFile = new File(DEFAULT_RESOURCES_LOCATION + DEFAULT_STOP_IGNORE_LIST_FILE_NAME)
  private var stopIgnoreList:Array[String] = Array()

  private val s = Source.fromFile(stopIgnoreListFile)

  s.getLines.drop(1).foreach((line) => {
    //drop first row and iterate through others
    try {
      val splitLine = line.split(",")
      stopIgnoreList :+ splitLine(0)
    }
    catch {
      case e: Exception => throw new Exception("Error reading stop ignore listfile. Error on line: " + line)
    }
  })
  println("Stop Ignore List Loaded")

  def getSet:Set[String] = {
    stopIgnoreList.toSet
  }
}
