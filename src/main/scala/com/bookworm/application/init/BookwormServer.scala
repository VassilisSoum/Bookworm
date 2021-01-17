package com.bookworm.application.init

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Sync}
import com.bookworm.application.config.Configuration
import com.bookworm.application.config.Configuration.{Config, DatabaseConfig}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

object BookwormServer {

  def createTransactor[F[_]: ConcurrentEffect: ContextShift]: Resource[F, (HikariTransactor[F], Config)] =
    for {
      config <- Configuration.load()
      ec <- ExecutionContexts.fixedThreadPool[F](config.database.threadPoolSize)
      blocker <- Blocker[F]
      transactor <- transactor(config.database, ec, blocker)
    } yield (transactor, config)

  def migrate[F[_]: ConcurrentEffect: ContextShift](transactor: HikariTransactor[F]): F[Unit] =
    transactor.configure { dataSource =>
      Sync[F].delay {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }

  private def transactor[F[_]: ConcurrentEffect: ContextShift](
    config: DatabaseConfig,
    executionContext: ExecutionContext,
    blocker: Blocker
  ): Resource[F, HikariTransactor[F]] =
    HikariTransactor.newHikariTransactor[F](
      config.driver,
      config.url,
      config.user,
      config.password,
      executionContext,
      blocker
    )
}
