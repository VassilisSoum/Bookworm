package com.bookworm.application.customers.adapter.repository

import cats.effect.{ContextShift, IO}
import com.bookworm.application.config.Configuration.AuthenticationTokensConfig
import com.bookworm.application.customers.adapter.stringCodec
import com.bookworm.application.customers.domain.model.{AuthenticationToken, CustomerId}
import com.bookworm.application.customers.domain.port.outbound.AuthenticationTokenRepository
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.connection.RedisClient
import dev.profunktor.redis4cats.effect.Log.NoOp._

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.duration.FiniteDuration

private[repository] class AuthenticationTokenRepositoryImpl @Inject() (
    authenticationTokensConfig: AuthenticationTokensConfig,
    redisClient: RedisClient
)(implicit CS: ContextShift[IO])
  extends AuthenticationTokenRepository[IO] {

  override def saveAuthenticationToken(authenticationToken: AuthenticationToken): IO[Unit] =
    Redis[IO].fromClient(redisClient, stringCodec).use { cmd =>
      cmd.setEx(
        s"${authenticationToken.customerId.id}${AuthenticationTokenRepositoryImpl.authenticationTokenSuffix}",
        authenticationToken.token,
        FiniteDuration(authenticationTokensConfig.expirationInSeconds, TimeUnit.SECONDS)
      )
    }

  override def removeAuthenticationTokenOfCustomerId(customerId: CustomerId): IO[Unit] =
    Redis[IO].fromClient(redisClient, stringCodec).use { cmd =>
      cmd
        .del(s"${customerId.id}${AuthenticationTokenRepositoryImpl.authenticationTokenSuffix}")
        .map(_ => ())
    }

  override def exists(authenticationToken: AuthenticationToken): IO[Boolean] =
    Redis[IO].fromClient(redisClient, stringCodec).use { cmd =>
      cmd.exists(s"${authenticationToken.customerId.id}${AuthenticationTokenRepositoryImpl.authenticationTokenSuffix}")
    }
}

object AuthenticationTokenRepositoryImpl {
  private val authenticationTokenSuffix = ":TOKEN"
}
