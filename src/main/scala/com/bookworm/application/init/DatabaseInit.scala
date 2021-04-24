package com.bookworm.application.init

import cats.effect.{Blocker, ContextShift, IO, Resource}
import com.bookworm.application.config.Configuration
import com.bookworm.application.config.Configuration.{Config, DatabaseConfig}
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

object DatabaseInit {

  def createTransactor(implicit contextShift: ContextShift[IO]): Resource[IO, (HikariTransactor[IO], Config)] =
    for {
      config <- Configuration.load()
      ec <- ThreadPoolCreator.createFixedThreadPool(config.database.threadPoolSize)
      blocker <- Blocker[IO]
      transactor <- transactor(config.database, ec, blocker)
    } yield (transactor, config)

  def migrate(transactor: HikariTransactor[IO]): IO[Unit] =
    transactor.configure { dataSource =>
      IO.delay {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }

  private def transactor(
    config: DatabaseConfig,
    executionContext: ExecutionContext,
    blocker: Blocker
  )(implicit contextShift: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] =
    HikariTransactor.newHikariTransactor[IO](
      config.driver,
      config.url,
      config.user,
      config.password,
      executionContext,
      blocker
    )
}
