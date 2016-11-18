package com.github.jw3.pipe

import akka.http.scaladsl.model.Uri
import eri.commons.config.SSConfig


object conf {
  private val pipe = new SSConfig("pipe")

  val host = pipe.host.as[String]
  val port = pipe.port.as[Int]
  val path = pipe.uri.as[String]
  val uri: Uri = s"/$path"
}
