package com.PredictionAlgorithm.Commons

import java.util.{GregorianCalendar, Calendar}

import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions

/**
 * Created by chrischivers on 19/07/15.
 */
object Commons {

  def getDayCode(arrivalTime: Long): String = {
    val cal: Calendar  = new GregorianCalendar();

    cal.setTimeInMillis(arrivalTime);
    cal.get(Calendar.DAY_OF_WEEK) match {
      case Calendar.MONDAY => "MON"
      case Calendar.TUESDAY=> "TUES"
      case Calendar.WEDNESDAY => "WED"
      case Calendar.THURSDAY=> "THUR"
      case Calendar.FRIDAY => "FRI"
      case Calendar.SATURDAY => "SAT"
      case Calendar.SUNDAY => "SUN"
    }

  }

  def getTimeOffset(existingTimeStamp:Long):Int = {
    val existingTime: Calendar = new GregorianCalendar();
    existingTime.setTimeInMillis(existingTimeStamp)

    val beginningOfDayTime: Calendar = new GregorianCalendar(existingTime.get(Calendar.YEAR), existingTime.get(Calendar.MONTH), existingTime.get(Calendar.DAY_OF_MONTH))
    ((existingTime.getTimeInMillis - beginningOfDayTime.getTimeInMillis)/1000).toInt
  }

  def getPointSequenceFromStopCode(route_ID: String, direction_ID: Int, stop_Code: String): Option[Int] = {
    val x = TFLDefinitions.StopToPointSequenceMap.get(route_ID, direction_ID, stop_Code)
    if (x.isEmpty) None
    else Some(x.get._1)
  }

  def getStopCodeFromPointSequence(route_ID: String, direction_ID: Int, pointSequence: Int): Option[String] = {
    val x = TFLDefinitions.PointToStopSequenceMap.get(route_ID, direction_ID, pointSequence)
    if (x.isEmpty) None
    else Some(x.get._1)
  }

}
