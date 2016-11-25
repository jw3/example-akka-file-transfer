package com.github.jw3.pipe

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.{ByteString, Timeout}
import com.github.jw3._
import com.github.jw3.pipe.Server._
import com.github.jw3.pipe.Tap.Initialize

import scala.concurrent.duration._
import scala.util.{Failure, Success}


object Server {
  def props(implicit mat: ActorMaterializer) = Props(new Server)

  def size(r: HttpResponse) = r.entity.contentLengthOption match {
    case Some(sz) ⇒ sz
    case _ ⇒ throw new RuntimeException("couldnt extract entity size")
  }
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
          val tap = context.actorOf(Tap.props)
          // need to tap this transfer with a actor as a listener to provide status to webhook endpoints
          val f = Source.single(HttpRequest(uri = source.conf.uri)).via(streams.source)
                  .alsoTo(Sink.foreach(r ⇒ tap ! Initialize(size(r))))
                  .map(r ⇒ Multipart.FormData(Multipart.FormData.BodyPart(source.conf.filename, HttpEntity(ContentTypes.`text/plain(UTF-8)`, size(r), r.entity.dataBytes.via(Tap.to(tap))), Map("filename" → source.conf.filename))))
                  .mapAsync(1)(r ⇒
                    Source.fromFuture(Marshal(r).to[RequestEntity])
                    .map(e ⇒ HttpRequest(method = HttpMethods.POST, uri = dest.conf.uri, entity = e))
                    .via(streams.dest).runWith(Sink.head)
                  )
                  .runWith(Sink.head)

          complete(f)
        }
      }
    }
  }

  def receive: Receive = {
    case _ ⇒
  }
}

object Tap {
  def props = Props(new Tap)
  def to(tap: ActorRef) = Flow[ByteString].alsoTo(Sink.actorRef(tap, Done))

  case class Initialize(sz: Long)
}

class Tap extends Actor {
  def receive: Receive = {
    case Initialize(sz) ⇒ context.become(ready(sz))
  }

  def ready(sz: Long): Receive = {
    var pos: Long = 0

    {
      case v: ByteString ⇒
        pos += v.length
        println(s"tapped[${v.utf8String}] [$pos/$sz]")

      case Done ⇒
        println("----TAP Complete----")
        self ! PoisonPill
    }
  }
}
