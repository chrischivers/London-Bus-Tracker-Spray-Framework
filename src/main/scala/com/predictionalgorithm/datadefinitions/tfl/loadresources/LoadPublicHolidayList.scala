package com.predictionalgorithm.datadefinitions.tfl.loadresources

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import com.predictionalgorithm.datadefinitions.LoadResourceFromSource
import grizzled.slf4j.Logger

import scala.io.BufferedSource

object LoadPublicHolidayList extends LoadResourceFromSource{

  override val bufferedSource: BufferedSource = DEFAULT_PUBLIC_HOLIDAY_LIST_FILE
  val logger = Logger[this.type]

  lazy val publicHolidayList:List[Date] = {
    var publicHolidayList:List[Date] = List()
    bufferedSource.getLines().drop(1).foreach((line) => {

      try {
        val sdf = new SimpleDateFormat("dd/M/yyyy", Locale.UK)
        val date = sdf.parse(line)
        publicHolidayList = publicHolidayList :+ date
      }
      catch {
        case e: Exception =>
          logger.error("Error reading public holiday list file. Error on line: " + line)
          throw new Exception("Error reading public holiday list file. Error on line: " + line)
      }
    })
    logger.info("Public holiday List Loaded")
    publicHolidayList
  }

}
