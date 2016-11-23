package com.github.jw3.source

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.{complete ⇒ _}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.util.Timeout
import com.github.jw3._

import scala.concurrent.duration._
import scala.util.{Failure, Success}


object Server {
  def props(implicit mat: ActorMaterializer) = Props(new Server)
}

class Server(implicit mat: ActorMaterializer) extends Actor with ActorLogging {
  import context.dispatcher


  override def preStart(): Unit = {
    log.debug("starting source server at {}:{}", source.conf.host, source.conf.port)

    Http()(context.system).bindAndHandle(routes, source.conf.host, source.conf.port).onComplete {
      case Success(b) =>
        log.debug("source server started")
      case Failure(e) =>
        log.error("failed to start source server", e)
    }
  }

  val routes = {
    implicit val timeout = Timeout(10 seconds)

    logRequest("---SOURCE---") {
      path(source.conf.path) {
        getFromFile(source.conf.file)
      } ~
      path("slow") {
        complete {
          HttpEntity.Default(ContentTypes.`text/plain(UTF-8)`, source.conf.file.length,
            FileIO.fromPath(source.conf.file.toPath, 1).map { v ⇒ Thread.sleep(100); v })
        }
      }
    }
  }

  def receive: Receive = {
    case _ ⇒
  }
}
