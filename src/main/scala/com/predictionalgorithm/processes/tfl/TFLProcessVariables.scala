package com.predictionalgorithm.processes.tfl

import com.predictionalgorithm.processes.ProcessVariablesInterface

/**
 * Created by chrischivers on 22/06/15.
 */
object TFLProcessVariables extends ProcessVariablesInterface{
  val TIMEOUT: Int = 2000
  val TIMEOUT_INCREMENT: Int = 1000
  val LINE_TOLERANCE_IN_RELATION_TO_CURRENT_TIME = 30 //in Seconds

}
