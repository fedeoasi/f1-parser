import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.fedeoasi",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "F1 Parser",
    libraryDependencies ++= Seq(
      pdfBox,
      scalaCsv,
      scalajHttp,
      scalaTest % Test
    )
  )
