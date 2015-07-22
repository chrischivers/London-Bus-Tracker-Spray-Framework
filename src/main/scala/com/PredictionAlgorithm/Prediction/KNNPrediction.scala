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
  val K_TIME_THRESHOLD_LIMIT = 10800
  //In Seconds
  val NEAREST_DAY_SAMPLE_TIMESPAN = 43200
  //In Seconds
  val NEAREST_DAY_AVG_DISTANCE_THRESHOLD = 15
  val K_ST_DEV_THRESHOLD_LIMIT = 500


  override def makePrediction(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Of_Week: String, timeOffset: Int): Option[Double] = {
    val startingPoint = Commons.getPointSequenceFromStopCode(route_ID, direction_ID, from_Point_ID).getOrElse(return None)
    val endingPoint = Commons.getPointSequenceFromStopCode(route_ID, direction_ID, to_Point_ID).getOrElse(return None)
    var accumulatedPrediction = 0.0
    for (i <- startingPoint until endingPoint) {
      val fromStopID = Commons.getStopCodeFromPointSequence(route_ID, direction_ID, i).getOrElse(return None)
      val toStopID = Commons.getStopCodeFromPointSequence(route_ID, direction_ID, i + 1).getOrElse(return None)
      accumulatedPrediction += makePredictionBetweenConsecutivePoints(route_ID, direction_ID, fromStopID, toStopID, day_Of_Week, timeOffset).getOrElse(return None)
    }
    Option(accumulatedPrediction)
  }

  private def makePredictionBetweenConsecutivePoints(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Of_Week: String, timeOffset: Int): Option[Double] = {

    assert(Commons.getPointSequenceFromStopCode(route_ID, direction_ID, from_Point_ID).get + 1 == Commons.getPointSequenceFromStopCode(route_ID, direction_ID, to_Point_ID).get)

    val query = MongoDBObject(coll.ROUTE_ID -> route_ID, coll.DIRECTION_ID -> direction_ID, coll.FROM_POINT_ID -> from_Point_ID, coll.TO_POINT_ID -> to_Point_ID)
    val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(query)



    if (cursor.size == 0) return None //If no entry in DB with route, direction, fromPoint and toPoint... return Nothing
    else {
      val dayDurTimeOffsetMap = getDayDurMapFromCursor(cursor) // Map is rDay of Week -> Vector of Duration, TimeOffset
      println("full dayDurTimeOffsetMap map: " + dayDurTimeOffsetMap)

      val dayDurTimeDifSortedMap = setTimeOffsetsToTimeAbsDifferencesAndSort(dayDurTimeOffsetMap, timeOffset)
      println("dayDurTimeDifSortedMap map: " + dayDurTimeDifSortedMap)

      val kNNListForAllDays = getKNNAllDays(dayDurTimeOffsetMap)
      println("K nearest neighbours for all days :" + kNNListForAllDays)

      val kNNAverageForEachDay = getAverageDurationForEachDay(kNNListForAllDays)
      println("Average duration for KNN of each day :" + kNNAverageForEachDay)

      val kNNSortedAverageDiferenceForEachDay = getSortedAverageDiferenceForEachDay(kNNAverageForEachDay,day_Of_Week)
      if (!kNNSortedAverageDiferenceForEachDay.isEmpty) assert(kNNSortedAverageDiferenceForEachDay.get(0)._1.equals(day_Of_Week))

      val kNNForThisDay = kNNListForAllDays.get(day_Of_Week)
      println("K nearest neighbours for this day (" + day_Of_Week + ") :" + kNNForThisDay)

      def expandFromOtherDays(KNNMap:Option[Vector[(Int, Int)]]):Option[Vector[(Int, Int)]] = {
       @tailrec
        def recursiveHelper(expandedKNNMap: Vector[(Int, Int)], acc: Int):Vector[(Int,Int)] = {
          if (expandedKNNMap.length >= K || acc >= 6) expandedKNNMap
          else {
            val daysNeeded = K - expandedKNNMap.length
            val kNNForThisDay = kNNListForAllDays.getOrElse(kNNSortedAverageDiferenceForEachDay.get(acc)._1, Vector()).take(daysNeeded)
            recursiveHelper(expandedKNNMap ++ kNNForThisDay, acc + 1)
          }
        }
        if (KNNMap.isEmpty) None
        else Option(recursiveHelper(KNNMap.get, 1))
      }

      val kNNExpandedFromOtherDays = expandFromOtherDays(kNNForThisDay)
      println("K nearest neighbours expand from other days: " + kNNExpandedFromOtherDays)
      //TODO what to do if KNN still not enough
      //TODO factor in degree of accuracy


      val averageDuration = getAverageForKNNVector(kNNExpandedFromOtherDays)
      println("Average Duration: " + averageDuration)

      averageDuration
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
    dayDurTimeDifSortedMap.map(kv => (kv._1, kv._2.take(K)))
  }

  private def getAverageDurationForEachDay(kNNAllDays: Map[String, Vector[(Int, Int)]]): Map[String, Double] = {
    // Map is Day of Week -> Vector of Duration, Time Difference
    kNNAllDays.map(kv => (kv._1, (kv._2.foldLeft(0.0)((acc,vec) => acc + vec._1)/kv._2.foldLeft(0.0)((acc,vec) => acc + 1))))
    //val averageForThisDayOfWeek = allDaysMap.get(dayOfWeek)
    //val allDaysMapS
  }

  private def getSortedAverageDiferenceForEachDay(kNNAllDaysAvgDur: Map[String, Double],dayOfWeek:String):Option[List[(String,Double)]] = {
    val dayOfWeekAvg = kNNAllDaysAvgDur.getOrElse(dayOfWeek,return None)
    Option(kNNAllDaysAvgDur.map(x => (x._1, math.abs(x._2 - dayOfWeekAvg))).toList.sortBy(_._2))
  }

  private def getAverageForKNNVector(knnVector:Option[Vector[(Int,Int)]]):Option[Double] = {
    //Vector of Duration, Time Difference
    if (knnVector.isEmpty) None
    else Some(knnVector.get.foldLeft(0.0)((acc,vec) => acc + vec._1)/knnVector.get.foldLeft(0.0)((acc,vec) => acc + 1))
  }

}



     
/*
      def getStandardDeviation(durationTimeDifVector: Vector[(Int, Int)]): Double = {
        val acc = new SummaryStatistics()
        durationTimeDifVector.foreach(x => acc.addValue(x._1))
        acc.getStandardDeviation
      }
*/

