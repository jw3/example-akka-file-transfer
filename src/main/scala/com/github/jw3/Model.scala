package com.github.jw3



object Model {
  case class HttpStart(host: String, port: Int = 8080)
  case class HttpStop()
}
