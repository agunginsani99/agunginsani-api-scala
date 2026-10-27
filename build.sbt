ThisBuild / scalaVersion := "3.3.6"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "agunginsani-api-scala",
    Compile / run / fork := true,
    Compile / run / javaOptions += "--sun-misc-unsafe-memory-access=allow",
    Test / fork := true,
    Test / javaOptions += "--sun-misc-unsafe-memory-access=allow",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.7.1",
      "org.http4s" %% "http4s-ember-server" % "0.23.30",
      "org.http4s" %% "http4s-dsl" % "0.23.30",
      "org.http4s" %% "http4s-circe" % "0.23.30",
      "io.circe"   %% "circe-generic" % "0.14.10",
      "com.google.apis" % "google-api-services-sheets" % "v4-rev20260930-2.0.0",
      "com.google.auth" % "google-auth-library-oauth2-http" % "1.54.0",
      "ch.qos.logback" % "logback-classic" % "1.2.13" % Runtime,
      "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test
    )
  )
