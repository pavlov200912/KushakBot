name := "untitled"

version := "0.1"

scalaVersion := "2.12.9"

libraryDependencies += "com.bot4s" %% "telegram-core" % "4.4.0-RC2"
libraryDependencies += "com.softwaremill.sttp" %% "json4s" % "1.7.2"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.0"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % "test"
libraryDependencies += "org.scalamock" %% "scalamock" % "4.4.0" % Test
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.3.1",
  "org.slf4j" % "slf4j-nop" % "1.7.26",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.1"
)

libraryDependencies += "com.h2database" % "h2" % "1.4.200"
libraryDependencies += "org.scalamock" %% "scalamock" % "4.4.0" % Test
