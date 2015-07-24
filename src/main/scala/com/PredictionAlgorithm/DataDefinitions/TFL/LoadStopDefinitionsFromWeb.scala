package com.PredictionAlgorithm.DataDefinitions.TFL

import java.io._
import java.net.{URL, HttpURLConnection}

import com.PredictionAlgorithm.DataDefinitions.LoadResource
import com.PredictionAlgorithm.DataDefinitions.TFL.LoadRouteDefinitionsFromWebsite._

import scala.io.Source


object LoadStopDefinitionsFromWeb extends LoadResource{




  // Maps StopCode -> (StopPointName;StopPointType;Towards;Bearing;StopPointIndicator;StopPointState;Latitude;Longitude)
  val stopDefinitionMap:Map[String,StopDefinitionFields] = Map()

  private def readStopDefinitions: Map[String,StopDefinitionFields]  = {

    lazy val stopList:Set[String] = TFLDefinitions.StopToPointSequenceMap.keySet.map {case(_,_,x) => x}
    var tempMap:Map[String,StopDefinitionFields] = Map()
    def tflURL(stopCode:String):String = "http://countdown.api.tfl.gov.uk/interfaces/ura/instant_V1?StopCode1=" + stopCode + "&ReturnList=StopPointName,StopPointType,Towards,Bearing,StopPointIndicator,StopPointState,Latitude,Longitude"

    println("Loading Stop Definitions From Web...")

    stopList.foreach { x =>
      val s = Source.fromURL(tflURL(x))
      s.getLines.drop(1).foreach(line => {
        val split = splitLine(line)
        tempMap += (x -> new StopDefinitionFields(split(0), split(1), split(2), split(3).toInt, split(4), split(5).toInt, split(6).toDouble, split(7).toDouble))
      }
      )
    }

    persist
    setUpdateVariable

    println("Stop definitons from web loaded")
    tempMap
  }

  private def splitLine(line: String) = line
    .substring(1,line.length-1) // remove leading and trailing square brackets,
    .split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)") // split at commas ignoring if in quotes (regEx taken from StackOverflow question: http://stackoverflow.com/questions/13335651/scala-split-string-by-commnas-ignoring-commas-between-quotes)
    .map(x => x.replaceAll("\"","")) //take out double quotations
    .map(x=> x.replaceAll(";",",")) //take out semicolons (causes errors on read of file)
    .tail // discards the first element (always '1')


  private def persist: Unit = {

    val LINE_SEPARATOR = "\r\n";

    val pw = new PrintWriter(new File(DEFAULT_RESOURCES_LOCATION + DEFAULT_STOP_DEFINITIONS_FILE_NAME))
    pw.write("BusStopCode;StopPointName;StopPointType;Towards;Bearing;StopPointIndicator;StopPointState;Latitude;Longitude"+ LINE_SEPARATOR) //Headers

    stopDefinitionMap.foreach{
      case ((stop_code),sdf: StopDefinitionFields) => {
        pw.write(stop_code + ";" + sdf.stopPointName+ ";" + sdf.stopPointType+ ";" + sdf.towards + ";" + sdf.bearing + ";" + sdf.stopPointIndicator + ";" + sdf.stopPointState + ";" + sdf.latitude + ";" + sdf.longitude + LINE_SEPARATOR)
      }
    }
    pw.close
    println("Stop definitions loaded from web persisted to file")
  }

  private def setUpdateVariable = {
    val file = new File(DEFAULT_RESOURCES_LOCATION + DEFAULT_VARIABLES_FILE_NAME)
    var tempStringArray:Array[String] = Array()
    val s = Source.fromFile(file)
    for (line <- s.getLines()) {
      if (line.startsWith(LAST_UPDATED_VARIABLE_NAME)) {
        tempStringArray = tempStringArray :+ (line.splitAt(LAST_UPDATED_VARIABLE_NAME.length + 1)._2 + System.currentTimeMillis())
      } else tempStringArray = tempStringArray :+ line
    }
    val pw = new PrintWriter(file)
    tempStringArray.foreach(x => pw.write(x))
    pw.close()
  }

}


