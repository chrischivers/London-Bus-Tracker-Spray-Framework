package com.PredictionAlgorithm.DataDefinitions

import com.PredictionAlgorithm.DataDefinitions.TFL.TFLDefinitions
import com.PredictionAlgorithm.Database.ROUTE_DEFINITIONS_COLLECTION
import com.PredictionAlgorithm.Database.TFL.TFLGetRouteDefinitionDocument

import scala.io.Source

/**
 * Created by chrischivers on 30/07/15.
 */
object getSubPoints {

  val collection = ROUTE_DEFINITIONS_COLLECTION
  var polySet:Set[(String,String,String)]= Set()

  def getAllRoutes = {
    val cursor = TFLGetRouteDefinitionDocument.fetchAllOrdered()
    var lastUpdatedStop = ""
    for (doc <- cursor) {
      if (doc.get(collection.FIRST_LAST).asInstanceOf[Option[String]].get == "FIRST") {
        lastUpdatedStop = doc.get(collection.STOP_CODE).asInstanceOf[String]
      }
      else {
      if (doc.get(collection.FIRST_LAST).asInstanceOf[Option[String]].get != "LAST") {
        val thisDoc = doc.get(collection.STOP_CODE).asInstanceOf[String]
        val lastStopCodeLat = TFLDefinitions.StopDefinitions(lastUpdatedStop).latitude
        val lastStopCodeLng = TFLDefinitions.StopDefinitions(lastUpdatedStop).longitude
        val thisStopCodeLat = TFLDefinitions.StopDefinitions(thisDoc).latitude
        val thisStopCodeLng = TFLDefinitions.StopDefinitions(thisDoc).longitude

        val url = "https://maps.googleapis.com/maps/api/directions/xml?origin=" + lastStopCodeLat + "," + lastStopCodeLng + "&destination=" + thisStopCodeLat + "," + thisStopCodeLng + "&key=AIzaSyAj6_kBtnllfulkTG0aih6onOnf9Qm5cX0&mode=driving"
        println(url)
        val s = Source.fromURL(url)
       // s.getLines().foreach()
      }
    }

  }
/*
    @Override
    public void run() {
      URL url = null;
      int i = 0;
      String SELECT_STATEMENT2 = "SELECT LINENAME, DIRECTIONID, SEQUENCE, BUS_STOP_CODE, XCORD, YCORD, NEXT_XCORD, NEXT_YCORD "
      + "FROM STOPINFORMATION "
      + "WHERE POLY_TO_NEXT_STOP_ENCODED IS NULL "
      + "ORDER BY LINENAME, DIRECTIONID, SEQUENCE";
      try {
        ps = connection.prepareStatement(SELECT_STATEMENT2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ps.executeQuery();
        while (rs.next()){

          String lineName = rs.getString("LINENAME");
          int direction = rs.getInt("DIRECTIONID");
          int sequenceNumber = rs.getInt("SEQUENCE");
          String busStopCode = rs.getString("BUS_STOP_CODE");
          BigDecimal originXCord = rs.getBigDecimal("XCORD");
          BigDecimal originYCord = rs.getBigDecimal("YCORD");
          BigDecimal destinationXCord = rs.getBigDecimal("NEXT_XCORD");
          BigDecimal destinationYCord = rs.getBigDecimal("NEXT_YCORD");

          // 0 is returned if null (i.e. end of route)
          if (destinationXCord != null && destinationYCord != null){
            System.out.println("Updating record: " + i);
            url = new URL("https://maps.googleapis.com/maps/api/directions/xml?origin=" + originXCord + "," + originYCord + "&destination=" + destinationXCord + "," + destinationYCord + "&key=AIzaSyAj6_kBtnllfulkTG0aih6onOnf9Qm5cX0&mode=driving");
            //url = new URL("https://maps.googleapis.com/maps/api/directions/xml?origin=" + originXCord + "," + originYCord + "&destination=" + destinationXCord + "," + destinationYCord + "&key=AIzaSyCHLODVvW1s20QhS_zyKEAYnlbvsC6Gu9w&mode=driving");
            //url = new URL("https://maps.googleapis.com/maps/api/directions/xml?origin=" + originXCord + "," + originYCord + "&destination=" + destinationXCord + "," + destinationYCord + "&key=AIzaSyAid_QWeBorH7qDSvyqoHubZp8kaVzxN7k&mode=driving");
            //url = new URL("https://maps.googleapis.com/maps/api/directions/xml?origin=" + originXCord + "," + originYCord + "&destination=" + destinationXCord + "," + destinationYCord + "&key=AIzaSyD-9dP1VD-Ok9-oY1aXhSZZCYR5CRo-Jus&mode=driving");
            //url = new URL("https://maps.googleapis.com/maps/api/directions/xml?origin=" + originXCord + "," + originYCord + "&destination=" + destinationXCord + "," + destinationYCord + "&key=AIzaSyDSEq-FMJhzFbQNIgK1JNQZuaLcPFV3oxw&mode=driving");
            //url = new URL("https://maps.googleapis.com/maps/api/directions/xml?origin=" + originXCord + "," + originYCord + "&destination=" + destinationXCord + "," + destinationYCord + "&key=AIzaSyDcuDPhqrEoVPoLxoeeLpWwx07fYjFqSeM&mode=driving");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String inputLine;
            reader.readLine(); //skip first line (header)

            boolean finishedScan = false;
            while ((inputLine = reader.readLine()) != null && !finishedScan) {
              // TODO check if below
              if (inputLine.contains("<overview_polyline>")) {
                inputLine = reader.readLine();
                // TODO
                String polyline = inputLine.substring(inputLine.indexOf("<points>")+8,inputLine.indexOf("</points>"));
                try {
                  String INSERT_POLYLINE_STATEMENT = "UPDATE STOPINFORMATION SET POLY_TO_NEXT_STOP_ENCODED = ? "
                  + "WHERE LINENAME = ? AND DIRECTIONID = ? AND SEQUENCE = ? AND BUS_STOP_CODE = ?";
                  ps2 = connection.prepareStatement(INSERT_POLYLINE_STATEMENT);
                  ps2.setString(1, polyline);
                  ps2.setString(2, lineName);
                  ps2.setInt(3, direction);
                  ps2.setInt(4, sequenceNumber);
                  ps2.setString(5, busStopCode);
                  ps2.executeUpdate();
                  finishedScan = true;
                } catch (SQLException e) {
                  e.printStackTrace();
                } finally {
                  ps2.close();
                }
              } else if (inputLine.contains("OVER_QUERY_LIMIT")) {
                throw new IllegalStateException("OVER QUERY LIMIT");
              }
            }
            if (!finishedScan) {
              System.out.println("Error: No Update Called on URL: " + url);
              System.out.println("No Update Called on Origin: " + originXCord + "," + originYCord + "," + destinationXCord + "," +  destinationYCord);
            }
            i++;
          }

        }


      } catch (SQLException e) {
        e.printStackTrace();
      } catch (MalformedURLException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } finally {
        try {
          ps.close();
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

  }
*/

}
