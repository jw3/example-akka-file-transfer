package com.github.jw3.source

import java.nio.file.Path

import akka.http.scaladsl.model.Uri
import eri.commons.config.SSConfig


object conf {
  private val source = new SSConfig("source")

  val host = source.host.as[String]
  val port = source.port.as[Int]
  val path = source.uri.as[String]
  val uri: Uri = s"/$path"

  val file = source.file.as[Path].toFile
  val filename = file.getName
}
