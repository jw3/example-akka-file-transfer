package com.github.jw3

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow

object streams {
  type Connection = Flow[HttpRequest, HttpResponse, _]

  def connection(host: String, port: Int = 8080, ssl: Boolean = false)(implicit system: ActorSystem): Connection = {
    if (ssl) Http().outgoingConnectionHttps(host, port)
    else Http().outgoingConnection(host, port)
  }

  def source(implicit system: ActorSystem): Connection = {
    import com.github.jw3.source.conf
    connection(conf.host, conf.port)
  }

  def pipe(implicit system: ActorSystem): Connection = {
    import com.github.jw3.pipe.conf
    connection(conf.host, conf.port)
  }

  def dest(implicit system: ActorSystem): Connection = {
    import com.github.jw3.dest.conf
    connection(conf.host, conf.port)
  }
}
