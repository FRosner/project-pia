//////////////////////////////
// Project Meta Information //
//////////////////////////////
organization  := "de.frosner"

version       := "0.1.0-SNAPSHOT"

name          := "project-pia"

scalaVersion  := "2.10.5"

/////////////////////
// Compile Options //
/////////////////////
fork in Compile := true

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

//////////////////////////
// Library Dependencies //
//////////////////////////
libraryDependencies ++= {
  val akkaVersion = "2.3.14"
  val akkaHttpVersion = "1.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaHttpVersion % "test",
    "com.github.lucarosellini.rJava" % "REngine" % "0.9-7",
    "com.github.lucarosellini.rJava" % "JRI" % "0.9-7",
    "com.github.lucarosellini.rJava" % "JRIEngine" % "0.9-7",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
}
