package com.predictionalgorithm.datasource


trait SourceLineFormatter {

  def apply(sourceLineString: String):SourceLine

}
