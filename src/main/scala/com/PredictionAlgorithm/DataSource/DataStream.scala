package com.PredictionAlgorithm.DataSource

trait DataStream {

  def getStream: Stream[String]

  def getNumberLinesToDisregard: Int

}
