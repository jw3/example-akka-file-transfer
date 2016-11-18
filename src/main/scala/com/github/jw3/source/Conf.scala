package com.github.jw3.source

import java.nio.file.Paths

import akka.http.scaladsl.model.Uri
import eri.commons.config.SSConfig


object conf {
  private val source = new SSConfig("source")

  val host = source.host.as[String]
  val port = source.port.as[Int]
  val path = source.uri.as[String]
  val uri: Uri = s"/$path"

  val dir = source.dir.as[String]
  val filename = source.filename.as[String]
  val file = Paths.get(dir, filename).toFile
}
