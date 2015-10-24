package com.predictionalgorithm.controlinterface.emailer

import javax.mail.internet.InternetAddress

import com.predictionalgorithm.commons.ReadProperties
import com.typesafe.scalalogging.LazyLogging
import courier.{Text, Mailer, Envelope}
import scala.concurrent.ExecutionContext.Implicits.global

object Emailer extends LazyLogging {

  val emailerFromAddress = ReadProperties.getProperty("emailerfromaddress")
  val emailerPassword = ReadProperties.getProperty("emailerpassword")
  val emailerToAddress = ReadProperties.getProperty("emailertoaddress")

  val mailer = Mailer("smtp.gmail.com", 587)
    .auth(true)
    .as(emailerFromAddress, emailerPassword)
    .startTtls(true)()

  def sendMesage(emailText:String) = {
    mailer(Envelope.from(new InternetAddress(emailerFromAddress))
      .to(new InternetAddress(emailerToAddress))
      .subject("BBKProject - Server Alert")
      .content(Text(emailText))).onSuccess {
      case _ => logger.info("Server Email sent :" + emailText)
    }
  }

}


