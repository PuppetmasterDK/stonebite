name := """stonebite"""
organization := "dk.lutzen"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// Configure Swagger
swaggerDomainNameSpaces := Seq("models")
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "dk.lutzen.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "dk.lutzen.binders._"
