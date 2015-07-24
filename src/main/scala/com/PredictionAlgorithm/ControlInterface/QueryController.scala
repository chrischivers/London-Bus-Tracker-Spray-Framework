package com.PredictionAlgorithm.ControlInterface

import com.PredictionAlgorithm.Prediction.{PredictionRequest, PredictionInterface, KNNPrediction}


class QueryController {

  //TODO get this by dependency injection
  val predictionAlgorithm:PredictionInterface = KNNPrediction

  def makePrediction(route_ID: String, direction_ID: Int, from_Point_ID: String, to_Point_ID: String, day_Of_Week: String, timeOffset: Int):String = {
    val option:Option[Double] = predictionAlgorithm.makePrediction(new PredictionRequest(route_ID,direction_ID,from_Point_ID,to_Point_ID,day_Of_Week,timeOffset))
    option.getOrElse("No predction available for these parameters").toString
  }

}
