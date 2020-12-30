val Http4sVersion = "0.21.11"
val CirceVersion = "0.13.0"
val Specs2Version = "4.10.5"
val LogbackVersion = "1.2.3"
val MacwireVersion = "2.3.7"
val ScalaMockVersion = "5.1.0"
val DoobieVersion = "0.9.4"
val PureConfigVersion = "0.14.0"
val FlywayVersion = "7.3.2"
val TestContainersVersion = "0.38.8"
val ScalaGuiceVersion = "4.2.11"

lazy val root = (project in file("."))
  .settings(
    organization := "com.bookworm",
    name := "bookworm",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.3",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-literal" % CirceVersion,
      "org.specs2" %% "specs2-core" % Specs2Version % Test,
      "org.specs2" %% "specs2-cats" % Specs2Version % Test,
      "org.scalamock" %% "scalamock" % ScalaMockVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
      "org.tpolecat" %% "doobie-specs2" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion,
      "net.codingwell" %% "scala-guice" % ScalaGuiceVersion,
      "org.flywaydb" % "flyway-core" % FlywayVersion,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % TestContainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % TestContainersVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test

    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )

Test / fork := true
