package com.github.jw3.pipe

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging


object Boot extends App with LazyLogging {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  val server = system.actorOf(Server.props)
}
