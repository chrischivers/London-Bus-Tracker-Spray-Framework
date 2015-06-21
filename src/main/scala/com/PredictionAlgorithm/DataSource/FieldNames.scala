package com.PredictionAlgorithm.DataSource

sealed trait FieldNames

trait POINT_ID extends FieldNames
trait ROUTE_ID extends FieldNames
trait DIRECTION_ID extends FieldNames
trait OBJECT_ID extends FieldNames
trait TIME_STAMP extends FieldNames