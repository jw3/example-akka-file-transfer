package com.github.jw3.dest

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import com.github.jw3.{dest, source}

import scala.concurrent.duration._
import scala.util.{Failure, Success}


object Server {
  def props(implicit mat: ActorMaterializer) = Props(new Server)
}

class Server(implicit mat: ActorMaterializer) extends Actor with ActorLogging {
  import context.dispatcher

  override def preStart(): Unit = {
    log.debug("starting dest server at {}:{}", dest.conf.host, dest.conf.port)

    Http()(context.system).bindAndHandle(routes, dest.conf.host, dest.conf.port).onComplete {
      case Success(b) =>
        log.debug("dest server started")
      case Failure(e) =>
        log.error("failed to start dest server", e)
    }
  }

  val routes = {
    implicit val timeout = Timeout(10 seconds)

    logRequest("---DEST---") {
      path(dest.conf.path) {
        post {
          fileUpload(source.conf.filename) { d ⇒
            val f = d._2.map(_.utf8String).runWith(Sink.head)
            complete(f)
          }
        }
      }
    }
  }

  def receive: Receive = {
    case _ ⇒
  }
}
