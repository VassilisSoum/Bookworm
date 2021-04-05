package com.bookworm.application.config

import cats.effect.{Blocker, ContextShift, IO, Resource}
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.module.catseffect.syntax._
import pureconfig.generic.auto._

object Configuration {
  case class ServerConfig(host: String, port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int)

  case class CustomerConfig(verificationTokenExpirationInSeconds: Long)

  case class Config(server: ServerConfig, database: DatabaseConfig, customer: CustomerConfig)

  def load(configFile: String = "application.conf")(implicit contextShift: ContextShift[IO]): Resource[IO, Config] =
    Blocker[IO].flatMap { blocker =>
      Resource.liftF(ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[IO, Config](blocker))
    }
}
