package com.PredictionAlgorithm.Prediction

import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Database.TFL.TFLGetPointToPointDocument
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.{MongoDBList, Imports, MongoDBObject}
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import scala.annotation.tailrec
import scala.collection.JavaConversions._


object KNNPrediction extends PredictionInterface {

  override val coll = POINT_TO_POINT_COLLECTION


  val K = 10
  val K_TIME_DIFFERENCE_THRESHOLD_LIMIT = 10800
  val MINIMUM_K_TO_MAKE_PREDICTION = 1
  //In Seconds



  override def makePrediction(pr:PredictionRequest): Option[Double] = {
    val startingPoint = Commons.getPointSequenceFromStopCode(pr.route_ID, pr.direction_ID, pr.from_Point_ID).getOrElse(return None)
    val endingPoint = Commons.getPointSequenceFromStopCode(pr.route_ID, pr.direction_ID, pr.to_Point_ID).getOrElse(return None)
    var accumulatedPrediction = 0.0
    var cumulativeDuration = pr.timeOffset.toDouble
    for (i <- startingPoint until endingPoint) {
      val fromStopID = Commons.getStopCodeFromPointSequence(pr.route_ID, pr.direction_ID, i).getOrElse(return None)
      val toStopID = Commons.getStopCodeFromPointSequence(pr.route_ID, pr.direction_ID, i + 1).getOrElse(return None)
      val duration = makePredictionBetweenConsecutivePoints(new PredictionRequest(pr.route_ID, pr.direction_ID, fromStopID, toStopID, pr.day_Of_Week, cumulativeDuration.toInt)).getOrElse(return None)
      accumulatedPrediction += duration
      cumulativeDuration += duration
    }
    Some(accumulatedPrediction)
  }

  def makePredictionBetweenConsecutivePoints(pr: PredictionRequest): Option[Double] = {

    assert(Commons.getPointSequenceFromStopCode(pr.route_ID, pr.direction_ID, pr.from_Point_ID).get + 1 == Commons.getPointSequenceFromStopCode(pr.route_ID, pr.direction_ID, pr.to_Point_ID).get)

    val query = MongoDBObject(coll.ROUTE_ID -> pr.route_ID, coll.DIRECTION_ID -> pr.direction_ID, coll.FROM_POINT_ID -> pr.from_Point_ID, coll.TO_POINT_ID -> pr.to_Point_ID)
    val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(query)



    if (cursor.size == 0) return None //If no entry in DB with route, direction, fromPoint and toPoint... return Nothing
    else {
      val dayDurTimeOffsetMap = getDayDurMapFromCursor(cursor) // Map is rDay of Week -> Vector of Duration, TimeOffset
      println("full dayDurTimeOffsetMap map: " + dayDurTimeOffsetMap)

      val dayDurTimeDifSortedMap = setTimeOffsetsToTimeAbsDifferencesAndSort(dayDurTimeOffsetMap, pr.timeOffset)
      println("dayDurTimeDifSortedMap map: " + dayDurTimeDifSortedMap)

      val kNNListForAllDays = getKNNAllDays(dayDurTimeDifSortedMap)
      println("K nearest neighbours for all days :" + kNNListForAllDays)

      val kNNAverageForEachDay = getAverageDurationForEachDay(kNNListForAllDays)
      println("Average duration for KNN of each day :" + kNNAverageForEachDay)

      val kNNSortedAverageDiferenceForEachDay = getSortedAverageDiferenceForEachDay(kNNAverageForEachDay,pr.day_Of_Week)
      if (!kNNSortedAverageDiferenceForEachDay.isEmpty) assert(kNNSortedAverageDiferenceForEachDay.get(0)._1.equals(pr.day_Of_Week))

      val kNNForThisDay = kNNListForAllDays.get(pr.day_Of_Week)
      println("K nearest neighbours for this day (" + pr.day_Of_Week + ") :" + kNNForThisDay)

      def expandFromOtherDays(KNNMap:Option[Vector[(Int, Int)]]):Option[Vector[(Int, Int)]] = {
       @tailrec
        def recursiveHelper(expandedKNNMap: Vector[(Int, Int)], acc: Int):Vector[(Int,Int)] = {
          if (expandedKNNMap.length >= K || acc >= kNNSortedAverageDiferenceForEachDay.get.length) expandedKNNMap
          else {
            val daysNeeded = K - expandedKNNMap.length
            val kNNForThisDay = kNNListForAllDays.getOrElse(kNNSortedAverageDiferenceForEachDay.get(acc)._1, Vector()).take(daysNeeded)
            recursiveHelper(expandedKNNMap ++ kNNForThisDay, acc + 1)
          }
        }
        if (KNNMap.isEmpty) None
        else Some(recursiveHelper(KNNMap.get, 1))
      }

      val kNNExpandedFromOtherDays = expandFromOtherDays(kNNForThisDay)
      println("K nearest neighbours expand from other days: " + kNNExpandedFromOtherDays)

       println("knn expanded length: " + kNNExpandedFromOtherDays.getOrElse(Vector()).length)
      if (kNNExpandedFromOtherDays.get.length >= MINIMUM_K_TO_MAKE_PREDICTION) {
        val averageDuration = getAverageForKNNVector(kNNExpandedFromOtherDays)
        println("Average Duration: " + averageDuration)
        averageDuration
      } else {
        None
      }
    }
  }

  private def getDayDurMapFromCursor(cursor: MongoCursor): Map[String, Vector[(Int, Int)]] = {
    // Map is Day of Week -> Vector of Duration, TimeOffset
    cursor.toList.map(x => {
      x.get(coll.DAY).asInstanceOf[String] -> {
        var vector: Vector[(Int, Int)] = Vector()
        x.get(coll.DURATION_LIST).asInstanceOf[Imports.BasicDBList].foreach(y => {
          vector = vector :+(y.asInstanceOf[Imports.BasicDBObject].getInt(coll.DURATION),
            y.asInstanceOf[Imports.BasicDBObject].getInt(coll.TIME_OFFSET))
        })
        vector.sortBy(_._2)
      }
    }).toMap
  }

  private def setTimeOffsetsToTimeAbsDifferencesAndSort(inMap: Map[String, Vector[(Int, Int)]], timeOffset: Int): Map[String, Vector[(Int, Int)]] = {
    // Map is Day of Week -> Vector of Duration, TimeOffset
    inMap.map(kv => (kv._1, kv._2.map { case (dur, time) => (dur, math.abs(timeOffset - time)) }.sortBy(_._2)))
  }

  private def getKNNAllDays(dayDurTimeDifSortedMap: Map[String, Vector[(Int, Int)]]): Map[String, Vector[(Int, Int)]] = {
    // Map is Day of Week -> Vector of Duration, Time Difference
    dayDurTimeDifSortedMap.map(kv => (kv._1, kv._2.filter(_._2 <= K_TIME_DIFFERENCE_THRESHOLD_LIMIT).take(K)))
     .filter(kv => kv._2.length > 0) //Remove empty entries
  }

  private def getAverageDurationForEachDay(kNNAllDays: Map[String, Vector[(Int, Int)]]): Map[String, Double] = {
    // Map is Day of Week -> Vector of Duration, Time Difference
    kNNAllDays.map(kv => (kv._1, (kv._2.foldLeft(0.0)((acc,vec) => acc + vec._1)/kv._2.foldLeft(0.0)((acc,vec) => acc + 1))))
    //val averageForThisDayOfWeek = allDaysMap.get(dayOfWeek)
    //val allDaysMapS
  }

  private def getSortedAverageDiferenceForEachDay(kNNAllDaysAvgDur: Map[String, Double],dayOfWeek:String):Option[List[(String,Double)]] = {
    val dayOfWeekAvg = kNNAllDaysAvgDur.getOrElse(dayOfWeek,return None)
    Some(kNNAllDaysAvgDur.map(x => (x._1, math.abs(x._2 - dayOfWeekAvg))).toList.sortBy(_._2))
  }

  private def getAverageForKNNVector(knnVector:Option[Vector[(Int,Int)]]):Option[Double] = {
    //Vector of Duration, Time Difference
    if (knnVector.isEmpty) None
    else Some(BigDecimal(knnVector.get.foldLeft(0.0)((acc,vec) => acc + vec._1)/knnVector.get.foldLeft(0.0)((acc,vec) => acc + 1)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble) //Includes rounding to 2 dp
  }

}



     
/*
      def getStandardDeviation(durationTimeDifVector: Vector[(Int, Int)]): Double = {
        val acc = new SummaryStatistics()
        durationTimeDifVector.foreach(x => acc.addValue(x._1))
        acc.getStandardDeviation
      }
*/

