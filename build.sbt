name := """stonebite"""
organization := "dk.lutzen"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// ScalaCheck
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
libraryDependencies += "org.scalamock" %% "scalamock" % "4.0.0" % Test

// Configure Swagger
swaggerDomainNameSpaces := Seq("models")

// Cats
libraryDependencies += "org.typelevel" %% "cats-core" % "1.0.1"

// mDNS
// https://mvnrepository.com/artifact/net.straylightlabs/hola
libraryDependencies += "net.straylightlabs" % "hola" % "0.2.2"

// Docker Settings
maintainer in Docker := "carsten@lutzen.dk"
dockerRepository := Some("puppetmasterdk")
dockerUpdateLatest := true

val regexPackageBase = "dk\\.lutzen"
coverageExcludedPackages :=
  List(
    s"$regexPackageBase\\.repository\\.Tables.*",
    s"$regexPackageBase\\.controllers\\.javascript\\.*",
    s"$regexPackageBase\\.controllers\\.Reverse.*",
    "controllers\\.Assets.*",
    "controllers\\.Reverse.*",
    "controllers\\.javascript\\.*",
    "router\\.*"
  ).mkString(";")

scalacOptions += "-Ypartial-unification"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "dk.lutzen.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "dk.lutzen.binders._"
