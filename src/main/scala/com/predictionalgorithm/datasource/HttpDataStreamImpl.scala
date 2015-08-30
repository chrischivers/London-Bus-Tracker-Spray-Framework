package com.predictionalgorithm.datasource

import java.io.{BufferedReader, InputStreamReader}
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClientBuilder}

/**
 * Code adapted from Stack Overflow: http://stackoverflow.com/questions/6024376/apache-httpcomponents-httpclient-timeout
 * @param ds The DataSource
 */
class HttpDataStreamImpl(ds: DataSource) extends DataStream{

  def getStream: Stream[String] = {

    def getCredentialsProvider = {
      val credentialsProvider = new BasicCredentialsProvider()
      val authScope = ds.AUTHSCOPE
      val credentials = new UsernamePasswordCredentials(ds.USERNAME,ds.PASSWORD)
      credentialsProvider.setCredentials(authScope,credentials)
      credentialsProvider
    }

    def getRequestBuilder = {
      val requestBuilder = RequestConfig.custom()
      requestBuilder.setConnectionRequestTimeout(ds.CONNECTION_TIMEOUT)
      requestBuilder.setConnectTimeout(ds.CONNECTION_TIMEOUT)
    }

    val client = HttpClientBuilder.create()
    client.setDefaultRequestConfig(getRequestBuilder.build())
    client.setDefaultCredentialsProvider(getCredentialsProvider)
    val httpGet = new HttpGet(ds.URL)
    val response = client.build().execute(httpGet)
    if (checkHttpStatusValid(response.getStatusLine.getStatusCode)) {
      val br = new BufferedReader(new InputStreamReader(response.getEntity.getContent))
      Stream.continually(br.readLine()).takeWhile(_ != null)
    } else {
      throw new IllegalStateException("Unable to retrieve input stream (http status not 200")
    }
  }

  private def checkHttpStatusValid(httpStatusCode: Int): Boolean = httpStatusCode == 200

  override def getNumberLinesToDisregard: Int = ds.NUMBER_LINES_TO_DISREGARD
}




