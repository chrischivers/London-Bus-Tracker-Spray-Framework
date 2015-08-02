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
      case Calendar.TUESDAY=> "TUE"
      case Calendar.WEDNESDAY => "WED"
      case Calendar.THURSDAY=> "THU"
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

  def decodePolyLine(encodedPolyLine: String): Vector[(String, String)] = {
    //Code adapted from Decode Method of Google's PolyUtil Class from Android Map Utils
    // https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java

    val len: Int = encodedPolyLine.length
    var latLngList: Vector[(String,String)] = Vector()

    var index: Int = 0
    var lat: Int = 0
    var lng: Int = 0

    while (index < len) {
      var result: Int = 1
      var shift: Int = 0
      var b: Int = 0

      do {
        b = encodedPolyLine.charAt(index) - 63 - 1
        index += 1
        result += b << shift
        shift += 5
      } while (b >= 0x1f)


      lat += (if ((result & 1) != 0) ~(result >> 1) else (result >> 1))

      result = 1
      shift = 0

      do {
        b = encodedPolyLine.charAt(index) - 63 - 1
        index += 1
        result += b << shift
        shift += 5
      } while (b >= 0x1f)

      lng += (if ((result & 1) != 0) ~(result >> 1) else (result >> 1))

      val x = ((lat * 1e-5).toString, (lng * 1e-5).toString)
      latLngList = latLngList :+ x
    }
    return latLngList
  }

}
