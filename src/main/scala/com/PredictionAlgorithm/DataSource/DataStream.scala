package com.PredictionAlgorithm.DataSource

/**
 * Created by chrischivers on 12/07/15.
 */
trait DataStream {

  def getStream: Stream[String]

  def getNumberLinesToDisregard: Int

}
