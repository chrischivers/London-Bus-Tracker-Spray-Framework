package com.predictionalgorithm.datadefinitions.tfl

import com.predictionalgorithm.datadefinitions.LoadResourceFromFile

import scala.io.BufferedSource


object LoadStopIgnoreList extends LoadResourceFromFile{

  override val bufferedSource: BufferedSource = DEFAULT_STOP_IGNORE_LIST_FILE

  lazy val stopIgnoreSet:Set[String] = {
   var stopIgnoreSet:Set[String] = Set()
    bufferedSource.getLines().drop(1).foreach((line) => {
      //drop first row and iterate through others
      try {
        val splitLine = line.split(",")
        stopIgnoreSet += splitLine(0)
      }
      catch {
        case e: Exception => throw new Exception("Error reading stop ignore listfile. Error on line: " + line)
      }
    })
    println("Stop Ignore List Loaded")
    stopIgnoreSet
  }

}
