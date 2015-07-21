package com.PredictionAlgorithm.Prediction

import com.PredictionAlgorithm.Commons.Commons
import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Database.TFL.TFLGetPointToPointDocument
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.{MongoDBList, Imports, MongoDBObject}
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
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

      val kNNListForThisDay: Vector[(Int, Int)] = getKNNForDay(dayDurTimeOffsetMap, day_Of_Week)
      println("K nearest neighbours for day of week (" + day_Of_Week + "): " + kNNListForThisDay)

      val averageDuration = calculateAverageforKNNList(kNNListForThisDay)

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
    inMap.map(day => (day._1, day._2.map { case (dur, time) => (dur, math.abs(timeOffset - time)) }.sortBy(_._2)))
  }

  private def getKNNForDay(dayDurTimeDifSortedMap: Map[String, Vector[(Int, Int)]], dayOfWeek: String): Vector[(Int, Int)] = {
    // Map is Day of Week -> Vector of Duration, Time Difference
    val thisDayVector = dayDurTimeDifSortedMap.getOrElse(dayOfWeek, return Vector()) //Vector of Duration, TimeOffset. Returns empty vector if day does not exist
    val kNearestNeighbours = thisDayVector.take(K) // TODO check K not too small
    kNearestNeighbours
  }


  private def calculateAverageforKNNList(kNNList: Vector[(Int, Int)]): Option[Double] = {
    if (!kNNList.isEmpty) {
      val averageDuration = kNNList.foldLeft(0.0)((acc, value) => acc + value._1) / kNNList.foldLeft(0.0)((acc, value) => acc + 1)
      Option(averageDuration)
    } else None
  }
}


/*

        val KNNwithClosestDaysApplied: Vector[(Int, Int)] = {
          var tempKNNList = KNNList
          if (tempKNNList.size < K) {
            val closestDaysList = getClosestDaysByAverage(dayDurTimeOffsetMap, timeOffset, day_Of_Week)
            if (!closestDaysList.isEmpty) {
              closestDaysList.get.foreach(x => {
                if (tempKNNList.size < K) {
                  tempKNNList :+ getDurationTimeDifferenceVector(getKNNForDay(dayDurTimeOffsetMap, x._1, timeOffset),timeOffset)
                }
              })
            }
          }
          tempKNNList
        }
          println("KNNList After closestDays applied: " + KNNwithClosestDaysApplied)


        //val sTDevOfDurations = getStandardDeviation(KNNList) //TODO check this is within threshold


      }
    }
    */

/*

      // Looks for clossest days by average (based on TIMESPAN constant) and returns list of (Day, Distance)
      def getClosestDaysByAverage(dayDurMap: Map[String, Vector[(Int, Int)]], timeOffset: Int, dayOfWeek: String): Option[List[(String, Double)]] = {
        val lowerThreshold = timeOffset - (NEAREST_DAY_SAMPLE_TIMESPAN / 2) //TODO could be zero...
        val upperthreshold = timeOffset + (NEAREST_DAY_SAMPLE_TIMESPAN / 2)
        val dayAverageMap = dayDurMap.map(x => {
          val durTimeOffsetVector: Vector[(Int, Int)] = x._2.filter(y => y._2 <= upperthreshold && y._2 >= lowerThreshold)
          println("durTimeOffsetVector vector:" + x._1 + " - " + durTimeOffsetVector)
          val averageDuration = durTimeOffsetVector.foldLeft(0.0)((acc, value) => acc + value._1) / durTimeOffsetVector.foldLeft(0.0)((acc, value) => acc + 1)
          (x._1, averageDuration)
        })
          .filter(_._2 > 0) // Filter out those days without any numeric values (or Not a Number values)
        println("day average map: " + dayAverageMap)
        if (dayAverageMap.contains(dayOfWeek)) {

          val dayDistanceMap = dayAverageMap.map { case (day, average) => (day, math.abs(dayAverageMap(dayOfWeek) - average)) }
          println("day distance map: " + dayDistanceMap)

          val sortedDayDistanceList = dayDistanceMap.toList.sortBy(_._2)
          assert(sortedDayDistanceList.get(0)._1.equals(dayOfWeek) && sortedDayDistanceList.get(0)._2 == 0.0)
          println("Sorted Day Distance List: " + sortedDayDistanceList)

          val filteredSortedDayDistanceList = sortedDayDistanceList.filter(x => x._2 < NEAREST_DAY_AVG_DISTANCE_THRESHOLD)
          assert(filteredSortedDayDistanceList.get(0)._1.equals(dayOfWeek) && filteredSortedDayDistanceList.get(0)._2 == 0.0)
          println("Filtered Sorted Day Distance List : " + filteredSortedDayDistanceList)

          Option(filteredSortedDayDistanceList)
        } else None

      }

     

      def getStandardDeviation(durationTimeDifVector: Vector[(Int, Int)]): Double = {
        val acc = new SummaryStatistics()
        durationTimeDifVector.foreach(x => acc.addValue(x._1))
        acc.getStandardDeviation
      }
*/

