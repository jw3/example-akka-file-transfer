package com.github.jw3.dest

import akka.http.scaladsl.model.Uri
import eri.commons.config.SSConfig


object conf {
  private val dest = new SSConfig("dest")

  val host = dest.host.as[String]
  val port = dest.port.as[Int]
  val path = dest.uri.as[String]
  val uri: Uri = s"/$path"
}
