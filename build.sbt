val Http4sVersion = "0.21.11"
val Specs2Version = "4.10.5"
val LogbackVersion = "1.2.3"
val MacwireVersion = "2.3.7"
val ScalaMockVersion = "5.1.0"
val DoobieVersion = "0.9.4"
val PureConfigVersion = "0.14.0"
val FlywayVersion = "7.3.2"
val TestContainersVersion = "0.38.8"
val ScalaGuiceVersion = "4.2.11"
val Json4sVersion = "3.6.10"
val CommonValidatorVersion = "1.7"
val AwsJavaSDK = "1.11.1000"
val GuavaVersion = "30.1.1-jre"
val ElasticMQVersion = "1.1.0"
val AkkaVersion = "2.6.13"
val AkkaHttpVersion = "10.2.4"
val AkkaStreamAlpakkaSQSVersion = "2.0.2"

lazy val root = (project in file("."))
  .settings(
    organization := "com.bookworm",
    name := "bookworm",
    version := "1.0",
    scalaVersion := "2.13.3",
    scalacOptions ++= Seq(
      "-encoding",
      "utf8",
      "-Xfatal-warnings",
      "-deprecation",
      "-unchecked",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials",
      "-language:postfixOps"
    ),
    scalacOptions += "-target:jvm-1.8",
    assemblyJarName in assembly := "bookworm.jar",
    assemblyMergeStrategy in assembly := {
      case "application.conf" => MergeStrategy.concat
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-json4s-jackson" % Http4sVersion,
      "org.json4s" %% "json4s-ext" % Json4sVersion,
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
      "commons-validator" % "commons-validator" % CommonValidatorVersion,
      "com.amazonaws" % "aws-java-sdk-sqs" % AwsJavaSDK,
      "com.amazonaws" % "aws-java-sdk-sns" % AwsJavaSDK,
      "com.amazonaws" % "aws-java-sdk-ses" % AwsJavaSDK,
      "com.google.guava" % "guava" % GuavaVersion,
      "org.elasticmq" %% "elasticmq-rest-sqs" % ElasticMQVersion,
      "com.lightbend.akka" %% "akka-stream-alpakka-sqs" % AkkaStreamAlpakkaSQSVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % TestContainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % TestContainersVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )

Test / fork := true
