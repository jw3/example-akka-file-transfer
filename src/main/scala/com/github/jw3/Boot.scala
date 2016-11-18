package com.github.jw3

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

object Boot extends App with LazyLogging {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  source.Boot.main(args)
  dest.Boot.main(args)
  pipe.Boot.main(args)
}
