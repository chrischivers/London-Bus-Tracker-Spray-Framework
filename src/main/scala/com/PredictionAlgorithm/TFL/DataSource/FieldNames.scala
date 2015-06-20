package com.PredictionAlgorithm.TFL.DataSource

sealed trait FieldName

final case object STOP_CODE extends FieldName
final case object BUS_ROUTE extends FieldName
final case object DIRECTION extends FieldName
final case object REG_NUMBER extends FieldName
final case object ARRIVAL_TIME extends FieldName