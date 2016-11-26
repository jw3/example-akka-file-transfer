package com.github.jw3.pipe

import java.util.UUID

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.{ByteString, Timeout}
import com.github.jw3._
import com.github.jw3.pipe.Server._
import com.github.jw3.pipe.Tap._

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
    implicit val timeout = Timeout(10 seconds)

    logRequest("---PIPE---") {
      get {
        pathPrefix("slow") {
          path(pipe.conf.path) {
            pipestream(s"/slow/${source.conf.path}")
          }
        } ~
        path(pipe.conf.path) {
          pipestream()
        } ~
        path("hook" / Segment) { streamId ⇒
          extractStream(streamId) { stream ⇒
            extractUpgradeToWebSocket { upgrade ⇒
              complete {
                val sink = Sink.actorRef[Message](stream, Mute(stream))
                val source = Source.actorRef[TextMessage](10, OverflowStrategy.fail).mapMaterializedValue(stream ! Listen(_))
                upgrade.handleMessages(Flow.fromSinkAndSource(sink, source))
              }
            }
          }
        }
      }
    }
  }

  def pipestream(src: Uri = source.conf.uri) = {
    import context.system

    val id = UUID.randomUUID.toString.take(7)
    println(s"creating stream: $id")
    val tap = context.actorOf(Tap.props, "abc")
    // need to tap this transfer with a actor as a listener to provide status to webhook endpoints
    val f = Source.single(HttpRequest(uri = src)).via(streams.source)
            .alsoTo(Sink.foreach(r ⇒ tap ! Initialize(size(r))))
            .map(r ⇒ Multipart.FormData(Multipart.FormData.BodyPart(source.conf.filename, HttpEntity(ContentTypes.`text/plain(UTF-8)`, size(r), r.entity.dataBytes.via(Tap.to(tap))), Map("filename" → source.conf.filename))))
            .mapAsync(1)(r ⇒ Marshal(r).to[RequestEntity].map(e ⇒ HttpRequest(method = HttpMethods.POST, uri = dest.conf.uri, entity = e)))
            .via(streams.dest)
            .runWith(Sink.head)

    complete(f)
  }

  def extractStream(streamId: String)(fn: ActorRef ⇒ Route)(implicit to: Timeout) = {
    onComplete(context.actorSelection(streamId).resolveOne()) {
      case Failure(_) ⇒ complete(StatusCodes.NotFound)
      case Success(m) ⇒ fn(m)
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
  case object TapComplete

  case class Listen(ref: ActorRef)
  case class Mute(ref: ActorRef)
}

class Tap extends Actor {
  def receive: Receive = {
    case Initialize(sz) ⇒ context.become(ready(sz))
  }

  def ready(sz: Long): Receive = {
    var listeners = Seq[ActorRef]()
    var pos: Long = 0

    {
      case v: ByteString ⇒
        pos += v.length

        val message = s"tapped[${v.utf8String}] [$pos/$sz]"
        println(message)
        listeners.foreach { l ⇒
          println("sending to listener")
          l ! TextMessage(message)
        }

      case TapComplete ⇒
        println("----TAP Complete----")
        self ! PoisonPill

      case Listen(ref) ⇒
        println("----Listen----")
        listeners ++= Seq(ref)

      case Mute(ref) ⇒
        println("----Mute----")
        listeners = listeners.filterNot(_ == ref)
    }
  }
}
