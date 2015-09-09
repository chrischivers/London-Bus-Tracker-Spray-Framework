package com.predictionalgorithm.datadefinitions

import java.util.Date

import com.predictionalgorithm.datadefinitions.tfl._
import com.predictionalgorithm.datadefinitions.tools.FetchPolyLines

/**
 * Created by chrischivers on 07/09/15.
 */
trait DataDefinitions {

  val RouteDefinitionMap:Map[(String, Int), List[(Int, String, Option[String], String)]]
  val PointDefinitionsMap: Map[String,StopDefinitionFields]


}
