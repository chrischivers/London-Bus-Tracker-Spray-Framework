package com.PredictionAlgorithm.Prediction

import com.PredictionAlgorithm.Database.POINT_TO_POINT_COLLECTION
import com.PredictionAlgorithm.Database.TFL.TFLGetPointToPointDocument
import com.mongodb.casbah.MongoCursor
import com.mongodb.casbah.commons.{MongoDBList, Imports, MongoDBObject}
import org.apache.commons.math3.stat.descriptive.SummaryStatistics
import scala.collection.JavaConversions._


object KNNPrediction extends PredictionInterface {

  val NEAREST_DAY_SAMPLE_TIMESPAN = 43200 //In Seconds
  val K = 10
  val K_TIME_THRESHOLD_LIMIT =  720000
  val K_ST_DEV_THRESHOLD_LIMIT = 500

  override val coll = POINT_TO_POINT_COLLECTION

  override def makePrediction(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Of_Week: String, timeOffset: Int): Option[Double] = {

    val query = MongoDBObject(coll.ROUTE_ID -> route_ID, coll.DIRECTION_ID -> direction_ID, coll.FROM_POINT_ID -> from_Point_ID, coll.TO_POINT_ID -> to_Point_ID)

    val cursor: MongoCursor = TFLGetPointToPointDocument.executeQuery(query)

    println(cursor.size)
    if (cursor.size == 0) return None //If no entry in DB with route, direction, fromPoint and toPoint... return Nothing
    else {
      val dayDurMap = getMapFromCursor(cursor) // Map is Day of Week -> Vector of Duration, TimeOffset
      //val closestDaysList = getClosestDaysByAverage(dayDurMap,timeOffset,day_Of_Week) //TODO implement
      val thisDayVector = dayDurMap(day_Of_Week) //Vector of Duration, TimeOffset
      val sortedNeighboursDifferences = getSortedNeighbours(thisDayVector,timeOffset)
      val kNearestNeighbours = sortedNeighboursDifferences.take(K) // TODO check K not too small
      val sTDevOfDurations = getStandardDeviation(kNearestNeighbours) //TODO check this is within threshold
      val averageDuration = kNearestNeighbours.foldLeft(0.0)((acc, value) => acc + value._1) / kNearestNeighbours.foldLeft(0.0)((acc, value) => acc + 1)
     Option(averageDuration)
    }
  }

  def getMapFromCursor(cursor:MongoCursor):Map[String, Vector[(Int, Int)]]  = {
    // Map is Day of Week -> Vector of Duration, TimeOffset
    var map: Map[String, Vector[(Int, Int)]] = Map()

    cursor.toList.foreach(x => {
      map += (x.get(coll.DAY).asInstanceOf[String] -> {

        //TODO see if this can be done with FoldLeft
        var vector: Vector[(Int, Int)] = Vector()
        x.get(coll.DURATION_LIST).asInstanceOf[Imports.BasicDBList].foreach(y => {
          vector = vector :+(y.asInstanceOf[Imports.BasicDBObject].getInt(coll.DURATION),
            y.asInstanceOf[Imports.BasicDBObject].getInt(coll.TIME_OFFSET))
        })
        println("full vector: " + x.get(coll.DAY) + " - " + vector)
        vector.sortBy(_._2)
      })
    })
    map
  }

  // Looks for clossest days by average (based on TIMESPAN constant) and returns list of (Day, Similarity)
  def getClosestDaysByAverage(dayDurMap: Map[String, Vector[(Int, Int)]], timeOffset: Int, dayOfWeek: String):Option[List[(String,Double)]] = {
    val lowerThreshold = timeOffset - (NEAREST_DAY_SAMPLE_TIMESPAN/2) //TODO could be zero...
    val upperthreshold = timeOffset + (NEAREST_DAY_SAMPLE_TIMESPAN/2)
    val dayAverageMap = dayDurMap.map(x => {
      val durTimeOffsetVector: Vector[(Int, Int)] = x._2.filter(y => y._2 <= upperthreshold && y._2 >= lowerThreshold)
      println("durTimeOffsetVector vector:" + x._1 + " - " + durTimeOffsetVector)
        val averageDuration = durTimeOffsetVector.foldLeft(0.0)((acc, value) => acc + value._1) / durTimeOffsetVector.foldLeft(0.0)((acc, value) => acc + 1)
        (x._1, averageDuration)
    })
      .filter(_._2 > 0) // Filter out those days without any numeric values (or Not a Number values)
    println("day average map: " + dayAverageMap)
      if (dayAverageMap.contains(dayOfWeek)) {
        val daySimilarityMap = dayAverageMap.map { case (day, average) => (day, math.abs(dayAverageMap(dayOfWeek) - average)) }
        println("day similarity map: " + daySimilarityMap)
        val sortedDaySimilarityList = daySimilarityMap.toList.sortBy(_._2)
        assert(sortedDaySimilarityList.get(0)._1.equals(dayOfWeek) && sortedDaySimilarityList.get(0)._2 == 0.0)
        println("Sorted Day SimlilarityList: " + sortedDaySimilarityList)
        Option(sortedDaySimilarityList)
      } else {
        None
      }

  }

  def getSortedNeighbours(thisDayVector: Vector[(Int, Int)], timeOffset: Int):Vector[(Int,Int)] = {
    //Vector of Duration, TimeOffset => Sorted Vector of Duration, Time Difference
    thisDayVector.map { case (dur,time) => (dur, math.abs(timeOffset - time))}.filter(_._2 <= K_TIME_THRESHOLD_LIMIT).sortBy(_._2)
  }

  def getStandardDeviation(durationTimeDifVector: Vector[(Int,Int)]):Double = {
    val acc = new SummaryStatistics()
    durationTimeDifVector.foreach(x => acc.addValue(x._1))
    acc.getStandardDeviation
  }
}
