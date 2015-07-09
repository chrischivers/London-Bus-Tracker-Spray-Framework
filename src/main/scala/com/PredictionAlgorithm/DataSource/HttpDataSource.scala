package com.PredictionAlgorithm.DataSource

import java.io.{BufferedReader, InputStreamReader}
import com.PredictionAlgorithm.DataSource.TFL.TFLDataSourceVariables
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClientBuilder}

/**
 * Created by chrischivers on 18/06/15.
 */
object HttpDataSource{

  val dsv = TFLDataSourceVariables //The variables file


  def getDataStream: Stream[String] = {

    def getCredentialsProvider = {
      val credentialsProvider = new BasicCredentialsProvider()
      val authScope = dsv.AUTHSCOPE
      val credentials = new UsernamePasswordCredentials(dsv.USERNAME,dsv.PASSWORD)
      credentialsProvider.setCredentials(authScope,credentials)
      credentialsProvider
    }

    def getRequestBuilder = {
      val requestBuilder = RequestConfig.custom()
      requestBuilder.setConnectionRequestTimeout(dsv.CONNECTION_TIMEOUT)
      requestBuilder.setConnectTimeout(dsv.CONNECTION_TIMEOUT)
    }

    val client = HttpClientBuilder.create()
    client.setDefaultRequestConfig(getRequestBuilder.build())
    client.setDefaultCredentialsProvider(getCredentialsProvider)
    val httpGet = new HttpGet(dsv.URL)
    val response = client.build().execute(httpGet)
    if (checkHttpStatusValid(response.getStatusLine.getStatusCode)) {
      val br = new BufferedReader(new InputStreamReader(response.getEntity.getContent))
      Stream.continually(br.readLine()).takeWhile(_ != null)
    } else {
      throw new IllegalStateException("Unable to retrieve input stream (http status not 200")
    }
  }

  private def checkHttpStatusValid(httpStatusCode: Int): Boolean = {
    (httpStatusCode == 200)
  }

}




