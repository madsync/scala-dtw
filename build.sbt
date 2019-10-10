import org.scoverage.coveralls.Imports.CoverallsKeys._


name := "scala-dtw"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.madsync" %% "joda-in-java-time" % "0.1.4",
  "org.scalatest" %% "scalatest" % "3.0.4" withSources() exclude("org.scala-lang", "scala-library") exclude("org.scala-lang", "scala-reflect")
)

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.1.0"

coverallsToken := Some("o4nLaD4B9eFFVEndv0Jdm1n4ke16n7iGL")