package com.predictionalgorithm.datasource


trait SourceLineProcessor {

  def apply(sourceLineString: String):SourceLine

}
