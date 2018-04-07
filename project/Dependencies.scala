import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val pdfBox = "org.apache.pdfbox" % "pdfbox" % "2.0.9"
  lazy val scalaCsv = "com.github.tototoshi" %% "scala-csv" % "1.3.5"
  lazy val scalajHttp = "org.scalaj" %% "scalaj-http" % "2.3.0"
}
