package com.predictionalgorithm.controlinterface.emailer

import javax.mail.internet.InternetAddress

import courier.{Text, Mailer, Envelope}
import grizzled.slf4j.Logger
import scala.concurrent.ExecutionContext.Implicits.global

object Emailer{

  val logger = Logger[this.type]

  val mailer = Mailer("smtp.gmail.com", 587)
    .auth(true)
    .as("tfllivebusmap@gmail.com", "dell2001")
    .startTtls(true)()

  def sendMesage(emailText:String) = {
    mailer(Envelope.from(new InternetAddress("tfllivebusmap@gmail.com"))
      .to(new InternetAddress("chrischivers@gmail.com"))
      .subject("BBKProject - Server Alert")
      .content(Text(emailText))).onSuccess {
      case _ => logger.info("Server Email sent :" + emailText)
    }
  }

}


