organization := "com.github.jw3"
name := "example-fileupload-pipe"
version := "1.0"

scalaVersion := "2.11.8"
scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",

  "-feature",
  "-unchecked",
  "-deprecation",

  "-language:postfixOps",
  "-language:implicitConversions",

  "-Ywarn-unused-import",
  "-Xfatal-warnings",
  "-Xlint:_"
)

libraryDependencies ++= {
  val akkaVersion = "2.4.14"
  val akkaHttpVersion = "10.0.0"
  val scalatestVersion = "3.0.0"

  Seq(
    "com.iheart" %% "ficus" % "1.2.6",
    "com.elderresearch" %% "ssc" % "0.2.0",

    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,

    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",

    "org.scalactic" %% "scalactic" % scalatestVersion % Test,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
  )
}
