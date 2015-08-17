package com.PredictionAlgorithm.Prediction

import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Database.TFL.TFLGetPointToPointDocument
import com.PredictionAlgorithm.Processes.Weather.Weather
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.{MongoDBList, Imports, MongoDBObject}
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import scala.annotation.tailrec
import scala.collection.JavaConversions._


object KNNPrediction extends PredictionInterface {

  override val coll = POINT_TO_POINT_COLLECTION


  val K = 10
  val MINIMUM_K_TO_MAKE_PREDICTION = 5 //In Seconds
  val WEIGHTING_TIME_OFFSET = 0.3
  val WEIGHTING_DAY_OF_WEEK = 0.3
  val WEIGHTING_RAINFALL = 0.3
  val WEIGHTING_RECENT = 0.1




  override def makePrediction(pr:PredictionRequest): Option[Double] = {
    val startingPoint = TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x=> x._2 == pr.from_Point_ID).head._1
    val endingPoint = TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x=> x._2 == pr.to_Point_ID).last._1
    var accumulatedPrediction = 0.0
    var cumulativeDuration = pr.timeOffset.toDouble
    for (i <- startingPoint until endingPoint) {
      val fromStopID = TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x => x._1 == i).head._2
      val toStopID = TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x => x._1 == i + 1).last._2
      val duration = makePredictionBetweenConsecutivePoints(new PredictionRequest(pr.route_ID, pr.direction_ID, fromStopID, toStopID, pr.day_Of_Week, cumulativeDuration.toInt)).getOrElse(return None)

      accumulatedPrediction += duration._1
      cumulativeDuration += duration._1

    }
    Some(round(accumulatedPrediction, 2))
  }

  def makePredictionBetweenConsecutivePoints(pr: PredictionRequest): Option[(Double, Double)] = {


//    assert(TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x=> x._2 == pr.from_Point_ID).head._1 + 1 == TFLDefinitions.RouteDefinitionMap(pr.route_ID, pr.direction_ID).filter(x=> x._2 == pr.to_Point_ID).head._1)

    val query = MongoDBObject(coll.ROUTE_ID -> pr.route_ID, coll.DIRECTION_ID -> pr.direction_ID, coll.FROM_POINT_ID -> pr.from_Point_ID, coll.TO_POINT_ID -> pr.to_Point_ID)
    val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(query)



    if (cursor.size == 0) return None //If no entry in DB with route, direction, fromPoint and toPoint... return Nothing
    else {
      getAverageAndSD(getSortedKNNDistances(cursor).take(K))

    }
  }

  //returns Array(Duration, WeightedDistance)
  private def getSortedKNNDistances(cursor: MongoCursor):Vector[(Int, Double)] = {



    val currentTimeOffset = Commons.getTimeOffset(System.currentTimeMillis())
    val currentRainFall = Weather.getCurrentRainfall
    val currentTime = System.currentTimeMillis()

    var weightedKNNArray:Vector[(Int, Double)] = Vector()

    cursor.foreach(x => {
      val day = x.get(coll.DAY).asInstanceOf[String]
      val differenceArray = x.get(coll.DURATION_LIST).asInstanceOf[Imports.BasicDBList].map(y =>
        (y.asInstanceOf[Imports.BasicDBObject].getInt(coll.DURATION),
        math.abs(y.asInstanceOf[Imports.BasicDBObject].getInt(coll.TIME_OFFSET) - currentTimeOffset),
        math.abs(y.asInstanceOf[Imports.BasicDBObject].getLong(coll.TIME_STAMP) - currentTime),
      math.abs(y.asInstanceOf[Imports.BasicDBObject].getDouble(coll.RAINFALL)) - currentRainFall))

      val maxTimeOffsetDifference = differenceArray.maxBy(_._2)._2.toDouble
      val maxTimeDifference = differenceArray.maxBy(_._3)._3.toDouble
      val maxRainfallDifference = differenceArray.maxBy(_._4)._4

      val durationDistanceArray:Vector[(Int, Double)] = differenceArray.map(z =>
        (z._1,
        (getDayOfWeekValue(day) * WEIGHTING_DAY_OF_WEEK) +
          ((z._2 / maxTimeOffsetDifference) * WEIGHTING_TIME_OFFSET) +
          ((z._3 / maxTimeDifference) * WEIGHTING_RECENT) +
          ((z._4 / maxRainfallDifference) * WEIGHTING_RAINFALL))).toVector

      weightedKNNArray = weightedKNNArray ++ durationDistanceArray
    })
      weightedKNNArray.sortBy(_._2)
  }

  private def getDayOfWeekValue(dayOfWeek:String):Double = {
    val weekDays = Vector("MON","TUE","WED","THU","FRI")
    val today = Commons.getDayCode(System.currentTimeMillis())

    if (today == dayOfWeek) 0.5
    else if(weekDays.contains(today) && weekDays.contains(dayOfWeek)) 0.6
    else 0.8

  }



  private def getAverageAndSD(durationDistanceVector:Vector[(Int,Double)]):Option[(Double, Double)] = {
    //Vector of Duration, Time Difference
    if (durationDistanceVector.isEmpty) None
    else {
      val acc = new SummaryStatistics()
      durationDistanceVector.foreach(x => acc.addValue(x._1))
      Some(acc.getMean, acc.getStandardDeviation)
    }
  }

  def round(number: Double, decimalPlaces: Int): Double = {
    BigDecimal(number).setScale(decimalPlaces, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  }