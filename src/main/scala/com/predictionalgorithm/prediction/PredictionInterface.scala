package com.predictionalgorithm.prediction

import com.predictionalgorithm.database.DatabaseCollections

trait PredictionInterface {

  val coll:DatabaseCollections

  def makePrediction (pR:PredictionRequest):Option[(Double, Double)]

}
