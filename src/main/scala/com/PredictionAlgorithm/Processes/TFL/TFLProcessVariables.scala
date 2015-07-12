package com.PredictionAlgorithm.Processes.TFL

import com.PredictionAlgorithm.Processes.ProcessVariablesInterface

/**
 * Created by chrischivers on 22/06/15.
 */
object TFLProcessVariables extends ProcessVariablesInterface{
  val TIMEOUT: Int = 2000
  val TIMEOUT_INCREMENT: Int = 1000
  val LINE_TOLERANCE_IN_RELATION_TO_CURRENT_TIME = 30000 //in ms

}
