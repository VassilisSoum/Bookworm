package com.bookworm.application.config

import cats.effect.{Blocker, ContextShift, Resource, Sync}
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.module.catseffect.syntax._
import pureconfig.generic.auto._

object Configuration {
  case class ServerConfig(host: String, port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int)

  case class Config(server: ServerConfig, database: DatabaseConfig)

  def load[F[_]: Sync: ContextShift](configFile: String = "application.conf"): Resource[F, Config] =
    Blocker[F].flatMap { blocker =>
      Resource.liftF(ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[F, Config](blocker))
    }
}
