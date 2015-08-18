package com.PredictionAlgorithm.Commons

import java.util.{GregorianCalendar, Calendar}

import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions

import scala.math.BigDecimal.RoundingMode


object Commons {

  val LABEL_DISTANCE = 40
  val LABEL_ROTATE_POINT_V_OFFSET = 10


  def getDayCode(arrivalTime: Long): String = {
    val cal: Calendar  = new GregorianCalendar()

    cal.setTimeInMillis(arrivalTime)
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
    val existingTime: Calendar = new GregorianCalendar()
    existingTime.setTimeInMillis(existingTimeStamp)

    val beginningOfDayTime: Calendar = new GregorianCalendar(existingTime.get(Calendar.YEAR), existingTime.get(Calendar.MONTH), existingTime.get(Calendar.DAY_OF_MONTH))
    ((existingTime.getTimeInMillis - beginningOfDayTime.getTimeInMillis)/1000).toInt
  }

  // Returns array of Lat, Lng, Rotation To Here, Proportional Distance To Here, Label Position To Here Lat, Label Position To Here Lng
  def getMovementDataArray(encodedPolyLine: String, routeID:String):Array[(String,String,String,String,String, String)] = {
    val decodedPolyLine = decodePolyLine(encodedPolyLine)

    var arrayBuild: Array[(Double,Double, Int, Double, Double, Double)] = Array()
    arrayBuild = arrayBuild :+ (decodedPolyLine(0)._1, decodedPolyLine(0)._2, 0, 0.0,0.0,0.0) //Initial entry for first point

    for(i <- 1 until decodedPolyLine.length) {
      val prevLat = decodedPolyLine(i - 1)._1
      val prevLng = decodedPolyLine(i - 1)._2
      val thisLat = decodedPolyLine(i)._1
      val thisLng = decodedPolyLine(i)._2
      val rotationToHere = getRotation(prevLat, prevLng, thisLat, thisLng)
      val distanceToHere = getDistance(prevLat, prevLng, thisLat, thisLng)
      val labelToHere = getLabelPosition(rotationToHere, routeID.length)
      arrayBuild = arrayBuild :+ (thisLat, thisLng, rotationToHere, distanceToHere, labelToHere._1, labelToHere._2) //Initial entry for first point

    }
    val sumOfDistances = arrayBuild.foldLeft(0.0) {(total, n) =>
      total + n._4
    }

    arrayBuild.map{case (lat,lng,rot,dist,labx,laby) =>
      (BigDecimal(lat).setScale(6, RoundingMode.HALF_UP).toString(),
        BigDecimal(lng).setScale(6, RoundingMode.HALF_UP).toString(),
        rot.toString,
        try {
          BigDecimal(dist / sumOfDistances).setScale(2, RoundingMode.HALF_UP).toString()
        } catch {
          case e:NumberFormatException => "0"
        },
        BigDecimal(labx).setScale(6, RoundingMode.HALF_UP).toString(),
        BigDecimal(laby).setScale(6, RoundingMode.HALF_UP).toString())}
  }


  def decodePolyLine(encodedPolyLine: String): Array[(Double, Double)] = {
    //Code adapted from Decode Method of Google's PolyUtil Class from Android Map Utils
    // https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java

    val len: Int = encodedPolyLine.length
    var latLngList: Array[(Double,Double)] = Array()

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

      //BigDecimal("3.53456").round(new MathContext(4, RoundingMode.HALF_UP));
      //val x = (BigDecimal(lat * 1e-5).setScale(6, RoundingMode.HALF_UP), BigDecimal(lng * 1e-5).setScale(6, RoundingMode.HALF_UP))
      val x = (lat * 1e-5, lng * 1e-5)
      latLngList = latLngList :+ x
    }
    return latLngList
  }

  def rad(x:Double) = x * Math.PI / 180

  // Not own code. Taken from: http://stackoverflow.com/questions/1502590/calculate-distance-between-two-points-in-google-maps-v3
  def getDistance(lat1:Double, lng1:Double, lat2:Double, lng2:Double): Double = {
    val R = 6378137 // Earth’s mean radius in meter
    val dLat = rad(lat2 - lat1)
    val dLong = rad(lng2 - lng2)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(rad(lat1)) * Math.cos(rad(lat2)) *
        Math.sin(dLong / 2) * Math.sin(dLong / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val d = R * c
    d
  }

  // Not my code
  // Formula from http://stackoverflow.com/questions/2908892/get-degrees-0-360-from-one-latlng-to-another-in-javascript
  def getRotation(lat1:Double, lng1:Double, lat2:Double, lng2:Double): Int = {
    val lat1x = lat1 * Math.PI / 180
    val lat2x = lat2 * Math.PI / 180
    val dLon = (lng2 - lng1) * Math.PI / 180

    val y = Math.sin(dLon) * Math.cos(lat2x);
    val x = Math.cos(lat1x) * Math.sin(lat2x) -
      Math.sin(lat1x) * Math.cos(lat2x) * Math.cos(dLon)

    val brng = Math.atan2(y, x)
    (((brng * 180 / Math.PI) + 360) % 360).toInt
  }

  def getLabelPosition(rotation:Int, routeIDLength:Int): (Double, Double) = {
   // val h = -(Math.sin(rad(rotation)) * -LABEL_DISTANCE)  //+ ((routeIDLength / 2) * 8)
   // val v = Math.cos(rad(rotation))  * -LABEL_DISTANCE
    return (0, 0)
  }

}
