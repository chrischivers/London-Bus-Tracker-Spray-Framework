package com.PredictionAlgorithm.Database

/**
 * Created by chrischivers on 20/06/15.
 */
sealed trait Databases {
  val name: String
}

final case object PREDICTION_DATABASE extends Databases {
  override val name: String = "PredictionDB"
}
