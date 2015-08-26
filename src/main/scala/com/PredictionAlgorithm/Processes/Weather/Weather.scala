package com.PredictionAlgorithm.Processes.Weather

import java.util.Calendar

import scala.io.Source


object Weather {

  private val WEATHER_API_URL = "http://api.openweathermap.org/data/2.5/forecast/city?id=2643743&mode=xml&APPID=e236bab1ce50fe2b7c7fd581b2e467f1"
  private val DEFAULT_IF_UNAVAILABLE = 0.0
  private val DEFAULT_VALID_TO_IF_UNAVAILABLE = 900000
  private var lastRainfall = 0.0
  private var lastValidTo: Long = 0

  def getCurrentRainfall = {
    if (lastValidTo - System.currentTimeMillis() < 0) {
      try {
        loadCurrentRainFallFromWeb()
      } catch {
        case e: Exception =>
          println("weather exception, using default")
          lastRainfall = DEFAULT_IF_UNAVAILABLE
          lastValidTo = System.currentTimeMillis() + DEFAULT_VALID_TO_IF_UNAVAILABLE
      }
      println("weather fetched: " + lastRainfall)
      lastRainfall
    } else lastRainfall
  }


  private def loadCurrentRainFallFromWeb() = {
    try {
      val s = Source.fromURL(WEATHER_API_URL)
      val line = s.getLines().next()

      def helper(startIndex: Int): Unit = {

        val timeFromStartPoint = line.indexOf("time from=", startIndex)
        val timeToStartPoint = line.indexOf("to=", timeFromStartPoint) + 4
        val year = line.substring(timeToStartPoint, timeToStartPoint + 4).toInt
        val month = line.substring(timeToStartPoint + 5, timeToStartPoint + 7).toInt
        val day = line.substring(timeToStartPoint + 8, timeToStartPoint + 10).toInt
        val hour = line.substring(timeToStartPoint + 11, timeToStartPoint + 13).toInt

        println(timeFromStartPoint + ", " + timeToStartPoint + ", " + year + ", " + month + ", " + day + ", " + hour)

        val cal = Calendar.getInstance()

        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (cal.getTimeInMillis - System.currentTimeMillis() < 0) helper(timeToStartPoint)
        else {
          if (line.indexOf("<precipitation></precipitation>", timeToStartPoint) != -1 && line.indexOf("<precipitation></precipitation>", timeToStartPoint) < line.indexOf("<precipitation unit", timeToStartPoint)) {
            println("No rainfall")
            lastRainfall = DEFAULT_IF_UNAVAILABLE
            lastValidTo = cal.getTimeInMillis
          } else {
            val lineStartPoint = line.indexOf("<precipitation unit", timeToStartPoint)
            val valueStartPoint = line.indexOf("value", lineStartPoint) + 7
            val valueEndPoint = line.indexOf("\"", valueStartPoint)
            val rainFallValue = line.substring(valueStartPoint, valueEndPoint).toDouble

            lastRainfall = rainFallValue
            lastValidTo = cal.getTimeInMillis
          }
        }

      }
      helper(0)
    }
    catch {
      case e: Exception =>
        println("Rainfall exception, using default value")
        lastRainfall = DEFAULT_IF_UNAVAILABLE
        lastValidTo = System.currentTimeMillis() + DEFAULT_VALID_TO_IF_UNAVAILABLE
    }
  }

}
