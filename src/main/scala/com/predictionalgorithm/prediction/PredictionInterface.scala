package com.predictionalgorithm.prediction

import com.predictionalgorithm.database.DatabaseCollections

/**
 * Interface for prediction functions to inherit
 */
trait PredictionInterface {

  val coll:DatabaseCollections

  def makePrediction (pR:PredictionRequest):Option[(Double, Double)]

}
