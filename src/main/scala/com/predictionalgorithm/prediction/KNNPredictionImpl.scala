package com.predictionalgorithm.prediction

import com.predictionalgorithm.commons.Commons._
import com.predictionalgorithm.datadefinitions.tfl.TFLDefinitions
import com.predictionalgorithm.database.POINT_TO_POINT_COLLECTION
import com.predictionalgorithm.database.tfl.TFLGetPointToPointDocument
import com.predictionalgorithm.processes.weather.Weather
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.{Imports, MongoDBObject}
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import scala.collection.JavaConversions._


object KNNPredictionImpl extends PredictionInterface {

  override val coll = POINT_TO_POINT_COLLECTION

  val K = 10
  val STANDARD_DEVIATION_MULTIPLIER_TO_EXCLUDE = 2
  val SECONDS_THRESHOLD_TO_REPORT = 30
  val WEIGHTING_TIME_OFFSET = 0.3
  val WEIGHTING_DAY_OF_WEEK = 0.3
  val WEIGHTING_RAINFALL = 0.2
  val WEIGHTING_RECENT = 0.2


  /**
   * Makes a prediction between two points (consecutive or non consecutive) using the kNN algorithm
   * @param pr A prediction request object encapsulating the required fields
   * @return An Option of predicted duration and standard deviation
   */
  override def makePrediction(pr: PredictionRequest): Option[(Double, Double)] = {
    try {
      val startingPoint = TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x => x._2 == pr.from_Point_ID).head._1
      val endingPoint = TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x => x._2 == pr.to_Point_ID).last._1

      var accumulatedPredictedDuration = 0.0
      var accumulatedVariance = 0.0
      var cumulativeDuration = pr.timeOffset.toDouble

      for (i <- startingPoint until endingPoint) {
        val fromStopID = TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x => x._1 == i).head._2
        val toStopID = TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x => x._1 == i + 1).last._2
        val duration = makePredictionBetweenConsecutivePoints(new PredictionRequest(pr.route_ID, pr.direction_ID, fromStopID, toStopID, pr.day_Of_Week, cumulativeDuration.toInt)).getOrElse(return None)
        accumulatedPredictedDuration += duration._1
        accumulatedVariance += duration._2
        cumulativeDuration += duration._1

      }
      val standardDeviation = math.sqrt(accumulatedVariance)
      Some(round(accumulatedPredictedDuration, 2), round(standardDeviation, 2))
    }
    catch {
      case nsee: NoSuchElementException => None
    }
  }

  /**
   * Makes a prediction between two consecutive points
   * @param pr A prediction request object encapsulating the required fields
   * @return an Option of predicted duration and variance
   */
  private def makePredictionBetweenConsecutivePoints(pr: PredictionRequest): Option[(Double, Double)] = {

    val query = MongoDBObject(coll.ROUTE_ID -> pr.route_ID, coll.DIRECTION_ID -> pr.direction_ID, coll.FROM_POINT_ID -> pr.from_Point_ID, coll.TO_POINT_ID -> pr.to_Point_ID)
    val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(query)



    if (cursor.size == 0) None //If no entry in DB with route, direction, fromPoint and toPoint... return Nothing
    else {
      val outliersRemoved = removeOutliers(getSortedKNNDistances(cursor).take(K))
      getAverageAndVariance(outliersRemoved)
    }
  }

  /**
   * Gets the sorted KNN distances for all records on the cursor
   * @param cursor A MongoCursor of the relevant records
   * @return A Vector of duratons and weighted distances
   */
  private def getSortedKNNDistances(cursor: MongoCursor): Vector[(Int, Double)] = {

    val currentTimeOffset = System.currentTimeMillis().getTimeOffset
    val currentRainFall = Weather.getCurrentRainfall
    val currentTime = System.currentTimeMillis()

    //var weightedKNNArray:Vector[(Int, Double)] = Vector()

    var rawValueArrayAllDays: Vector[(Int, Int, Double, Double, Long)] = Vector()

    cursor.foreach(x => {
      val day = getDayOfWeekValue(x.get(coll.DAY).asInstanceOf[String])
      val rawValueArray = x.get(coll.DURATION_LIST).asInstanceOf[Imports.BasicDBList].map(y =>
        (y.asInstanceOf[Imports.BasicDBObject].getInt(coll.DURATION),
          y.asInstanceOf[Imports.BasicDBObject].getInt(coll.TIME_OFFSET),
          day,
          y.asInstanceOf[Imports.BasicDBObject].getDouble(coll.RAINFALL),
          y.asInstanceOf[Imports.BasicDBObject].getLong(coll.TIME_STAMP)))

      rawValueArrayAllDays = rawValueArrayAllDays ++ rawValueArray
    })

    val minTimeOffset = rawValueArrayAllDays.minBy(_._2)._2.toDouble
    val maxTimeOffset = rawValueArrayAllDays.maxBy(_._2)._2.toDouble

    val minDayOfWeek = rawValueArrayAllDays.minBy(_._3)._3
    val maxDayOfWeek = rawValueArrayAllDays.maxBy(_._3)._3

    val minRainfall = rawValueArrayAllDays.minBy(_._4)._4
    val maxRainfall = rawValueArrayAllDays.maxBy(_._4)._4


    val minTimeDifference = rawValueArrayAllDays.minBy(_._5)._5.toDouble
    val maxTimeDifference = rawValueArrayAllDays.maxBy(_._5)._5.toDouble

    val currentTimeOffsetNormalised = normaliseInt(currentTimeOffset, minTimeOffset, maxTimeOffset)
    val currentDayNormalised = 0
    val currentRainFallNormalised = normaliseDouble(currentRainFall, minRainfall, maxRainfall)
    val currentTimeNormalised = normaliseLong(currentTime, minTimeDifference, maxTimeDifference)

    //println("raw valuea array: "+ rawValueArrayAllDays)

    val normalisedArray = rawValueArrayAllDays.map { case (duration, timeOffset, day, rainfall, timeStamp) =>
      (duration,
        normaliseInt(timeOffset, minTimeOffset, maxTimeOffset),
        normaliseDouble(day, minDayOfWeek, maxDayOfWeek),
        normaliseDouble(rainfall, minDayOfWeek, maxDayOfWeek),
        normaliseLong(timeStamp, minTimeDifference, maxTimeDifference))
    }
    // println("normalised array: " + normalisedArray)

    val durationWeightedDistanceArray = normalisedArray.map { case (duration, timeOffset, day, rainfall, timeStamp) =>
      (duration, math.sqrt(
        WEIGHTING_TIME_OFFSET * math.pow(currentTimeOffsetNormalised - timeOffset, 2) +
          WEIGHTING_DAY_OF_WEEK * math.pow(currentDayNormalised - day, 2) +
          WEIGHTING_RAINFALL * math.pow(currentRainFallNormalised - rainfall, 2) +
          WEIGHTING_RECENT * math.pow(currentTimeNormalised - timeStamp, 2)))
    }

    val sortedDurationWeightedDistanceArray = durationWeightedDistanceArray.sortBy(_._2)

    //  println("sortedDurationWeightedDistanceArray array: " + sortedDurationWeightedDistanceArray)

    sortedDurationWeightedDistanceArray
  }

  /**
   * Gets the distance value of the day of the week in relation to the current day
   * @param dayOfWeek The day of the week to compare today to
   * @return The distance value
   */
  private def getDayOfWeekValue(dayOfWeek: String): Double = {

    val SAME_DAY_VALUE = 0
    val EQUIV_DAY_WEEK_VALUE = 0.5
    val UNEQUIV_DAY_WEEK_VALUE = 1

    val weekDays = Vector("MON", "TUE", "WED", "THU", "FRI")
    val today = System.currentTimeMillis().getDayCode

    if (today == dayOfWeek) SAME_DAY_VALUE
    else if (weekDays.contains(today) && weekDays.contains(dayOfWeek)) EQUIV_DAY_WEEK_VALUE
    else UNEQUIV_DAY_WEEK_VALUE

  }

  private def normaliseInt(x: Int, min: Double, max: Double): Double = (x - min) / (max - min)

  private def normaliseDouble(x: Double, min: Double, max: Double): Double = (x - min) / (max - min)

  private def normaliseLong(x: Long, min: Double, max: Double): Double = (x - min) / (max - min)


  /**
   * Gets the average of a number of durations between points, plus the variance for each
   * @param durationDistanceVector A vector of Duration and Distance between a number of points
   * @return An Option of the average and the Variance
   */
  private def getAverageAndVariance(durationDistanceVector: Vector[(Int, Double)]): Option[(Double, Double)] = {
    if (durationDistanceVector.isEmpty) None
    else {
      val acc = new SummaryStatistics()
      durationDistanceVector.foreach(x => acc.addValue(x._1))
      Some(acc.getMean, acc.getVariance)
    }
  }

  /**
   * Removes outlienrs from the durationDistanceVector based on the Standard Devitation Multiplier
   * @param durationDistanceVector The vecotr of Durations and KNN Distances
   * @return A vector of Durations and KNN distances with the outliers removed
   */
  private def removeOutliers(durationDistanceVector: Vector[(Int, Double)]): Vector[(Int, Double)] = {
    if (durationDistanceVector.isEmpty) None
    val acc = new SummaryStatistics()
    durationDistanceVector.foreach(x => acc.addValue(x._1))
    val SD = acc.getStandardDeviation
    val mean = acc.getMean
    val SDmultiplier = SD * STANDARD_DEVIATION_MULTIPLIER_TO_EXCLUDE
    val durationUpperTolerance = mean + SDmultiplier
    val durationLowerTolerance = mean - SDmultiplier
    durationDistanceVector.filter(x => x._1 <= durationUpperTolerance && x._1 >= durationLowerTolerance)

  }

  /**
   * Rounds a Double to a fixed number of decimal places
   * @param number The input number
   * @param decimalPlaces The number of decimal places
   * @return The result
   */
  private def round(number: Double, decimalPlaces: Int): Double = {
    BigDecimal(number).setScale(decimalPlaces, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

}