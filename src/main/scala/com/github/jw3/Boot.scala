package com.github.jw3

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

object Boot extends App with LazyLogging {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  source.Boot.main(Array())
  dest.Boot.main(Array())
  pipe.Boot.main(Array())
}
