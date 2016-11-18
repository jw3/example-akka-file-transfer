package com.github.jw3

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import com.github.jw3.dest.{conf ⇒ destcfg}
import com.github.jw3.pipe.{conf ⇒ pipecfg}
import com.github.jw3.source.{conf ⇒ sourcecfg}

object streams {
  type Connection = Flow[HttpRequest, HttpResponse, _]

  def source(implicit system: ActorSystem) = connection(sourcecfg.host, sourcecfg.port)
  def pipe(implicit system: ActorSystem) = connection(pipecfg.host, pipecfg.port)
  def dest(implicit system: ActorSystem) = connection(destcfg.host, destcfg.port)

  def connection(host: String, port: Int = 8080)(implicit system: ActorSystem): Connection = Http().outgoingConnection(host, port)
}
