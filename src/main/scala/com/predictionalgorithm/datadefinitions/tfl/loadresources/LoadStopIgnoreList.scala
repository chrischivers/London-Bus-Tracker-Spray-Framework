package com.predictionalgorithm.datadefinitions.tfl.loadresources

import com.predictionalgorithm.datadefinitions.LoadResourceFromSource
import grizzled.slf4j.Logger

import scala.io.BufferedSource


object LoadStopIgnoreList extends LoadResourceFromSource{

  override val bufferedSource: BufferedSource = DEFAULT_STOP_IGNORE_LIST_FILE
  val logger = Logger[this.type]

  lazy val stopIgnoreSet:Set[String] = {
   var stopIgnoreSet:Set[String] = Set()
    bufferedSource.getLines().drop(1).foreach((line) => {
      //drop first row and iterate through others
      try {
        val splitLine = line.split(",")
        stopIgnoreSet += splitLine(0)
      }
      catch {
        case e: Exception =>
          logger.error("Error reading stop ignore listfile. Error on line: " + line)
          throw new Exception("Error reading stop ignore listfile. Error on line: " + line)
      }
    })
    logger.info("Stop Ignore List Loaded")
    stopIgnoreSet
  }

}
