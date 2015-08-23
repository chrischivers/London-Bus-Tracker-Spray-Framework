package com.PredictionAlgorithm.Prediction

import com.PredictionAlgorithm.Database.DatabaseCollections

trait PredictionInterface {

  val coll:DatabaseCollections

  def makePrediction (pR:PredictionRequest):Option[Double]

}
