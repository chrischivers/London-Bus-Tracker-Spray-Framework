package com.PredictionAlgorithm.Spray

import java.util.concurrent.LinkedBlockingQueue

import com.PredictionAlgorithm.Streaming.PackagedStreamObject


class FIFOStreamImplementation {

  // Implementation adapted from Stack Overflow article:
  //http://stackoverflow.com/questions/7553270/is-there-a-fifo-stream-in-scala
    private val queue = new LinkedBlockingQueue[Option[PackagedStreamObject]]

    def toStream: Stream[PackagedStreamObject] = queue take match {
      case Some(pso: PackagedStreamObject) => Stream cons(pso, toStream)
      case None => Stream empty
    }

    def close() = queue add None

    def enqueue(pso: PackagedStreamObject) = queue add Some(pso)

}