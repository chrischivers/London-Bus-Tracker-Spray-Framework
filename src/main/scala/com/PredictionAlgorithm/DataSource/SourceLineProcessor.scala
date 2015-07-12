package com.PredictionAlgorithm.DataSource

/**
 * Created by chrischivers on 12/07/15.
 */
trait SourceLineProcessor {

  def apply(sourceLineString: String):SourceLine

}
