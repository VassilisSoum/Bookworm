package com.bookworm.application.config

import cats.effect.{Blocker, ContextShift, IO, Resource}
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.module.catseffect.syntax._
import pureconfig.generic.auto._

object Configuration {
  case class ServerConfig(host: String, port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int)

  case class CustomerRegistrationVerificationConfig(
      senderEmail: String,
      registrationVerificationEmailTemplateName: String,
      threadPoolSize: Int
  )

  case class AwsConfig(awsRegion: String, sesConfigurationSet: String)

  case class CustomerConfig(
      verificationTokenExpirationInSeconds: Long,
      customerRegistrationVerificationConfig: CustomerRegistrationVerificationConfig
  )

  case class ExpiredVerificationTokensSchedulerConfig(enabled: Boolean, periodInMillis: Long)

  case class AuthenticationTokensConfig(expirationInSeconds: Int, jwtSecretKey: String)

  case class RedisConfig(url: String)

  case class Config(
      server: ServerConfig,
      database: DatabaseConfig,
      customer: CustomerConfig,
      expiredVerificationTokensScheduler: ExpiredVerificationTokensSchedulerConfig,
      authenticationTokensConfig: AuthenticationTokensConfig,
      aws: AwsConfig,
      redis: RedisConfig
  )

  def load(configFile: String = "application.conf")(implicit contextShift: ContextShift[IO]): Resource[IO, Config] =
    Blocker[IO].flatMap { blocker =>
      Resource.eval(ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[IO, Config](blocker))
    }
}
