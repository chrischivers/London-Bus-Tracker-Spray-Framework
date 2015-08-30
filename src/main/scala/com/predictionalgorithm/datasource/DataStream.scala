package com.predictionalgorithm.datasource

trait DataStream {

  def getStream: Stream[String]

  def getNumberLinesToDisregard: Int

}
