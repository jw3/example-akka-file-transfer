package com.github.jw3.pipe

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
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
    log.debug("starting pipe server at {}:{}", pipe.conf.host, pipe.conf.port)

    Http()(context.system).bindAndHandle(routes, pipe.conf.host, pipe.conf.port).onComplete {
      case Success(b) =>
        log.debug("pipe server started")
      case Failure(e) =>
        log.error("failed to start pipe server", e)
    }
  }

  val routes = {
    import context.system
    implicit val timeout = Timeout(10 seconds)

    logRequest("---PIPE---") {
      pathPrefix(pipe.conf.path) {
        get {
          val f = Source.single(HttpRequest(uri = source.conf.uri)).via(streams.source)
                  .map(r ⇒ Multipart.FormData(Multipart.FormData.BodyPart(source.conf.filename, HttpEntity(ContentTypes.`text/plain(UTF-8)`, size(r), r.entity.dataBytes), Map("filename" → source.conf.filename))))
                  .mapAsync(1)(r ⇒ Marshal(r).to[RequestEntity])
                  .mapAsync(1)(r ⇒ Source.single(HttpRequest(method = HttpMethods.POST, uri = dest.conf.uri, entity = r)).via(streams.dest).runWith(Sink.head))
                  .runWith(Sink.head)

          complete(f)
        }
      }
    }
  }

  def size(r: HttpResponse) = r.entity.contentLengthOption match {
    case Some(sz) ⇒ sz
    case _ ⇒ throw new RuntimeException("couldnt extract entity size")
  }

  def receive: Receive = {
    case _ ⇒
  }
}
