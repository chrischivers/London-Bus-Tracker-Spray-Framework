package com.PredictionAlgorithm.DataSource


trait SourceLineProcessor {

  def apply(sourceLineString: String):SourceLine

}
