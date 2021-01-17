package com.bookworm.application.init

import cats.effect.{Async, Blocker, ConcurrentEffect, ContextShift, ExitCode, Resource, Sync, Timer}
import cats.implicits._
import com.bookworm.application.books.adapter.api.{BookRestApi, RestModule}
import com.bookworm.application.books.adapter.repository.RepositoryModule
import com.bookworm.application.books.adapter.repository.dao.DaoModule
import com.bookworm.application.books.adapter.service.ServiceModule
import com.bookworm.application.config.Configuration
import com.bookworm.application.config.Configuration.{Config, DatabaseConfig}
import com.google.inject.{AbstractModule, Guice, Key, TypeLiteral}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import fs2.Stream
import net.codingwell.scalaguice
import net.codingwell.scalaguice.ScalaModule
import org.flywaydb.core.Flyway
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

object BookwormServer {

  def stream[F[_]: ConcurrentEffect: ContextShift: Timer]: F[Stream[F, ExitCode]] =
    resources.use(E => create(E))

  private def resources[F[_]: ConcurrentEffect: ContextShift]: Resource[F, (HikariTransactor[F], Config)] =
    for {
      config <- Configuration.load()
      ec <- ExecutionContexts.fixedThreadPool[F](config.database.threadPoolSize)
      blocker <- Blocker[F]
      transactor <- transactor(config.database, ec, blocker)
    } yield (transactor, config)

  private def create[F[_]: ConcurrentEffect: ContextShift: Timer](
    resources: (HikariTransactor[F], Config)
  ): F[Stream[F, ExitCode]] = {
    val injector = Guice.createInjector(
      new RestModule(),
      new ServiceModule(),
      new RepositoryModule(),
      new DaoModule(),
      new AbstractModule with ScalaModule {
        override def configure(): Unit =
          bind(new TypeLiteral[HikariTransactor[F]] {}).toInstance(resources._1)
      }
    )
    for {
      _ <- initialize(resources._1)
      httpApp = Logger.httpApp(logHeaders = true, logBody = true)(
        injector.getInstance(Key.get(scalaguice.typeLiteral[BookRestApi[F]])).getAllBooks /*<+>*/ .orNotFound
      )
      exitCode <- Async[F].delay(BlazeServerBuilder[F](global)
        .bindHttp(resources._2.server.port, resources._2.server.host)
        .withHttpApp(httpApp)
        .serve)
    } yield exitCode
  }

  private def transactor[F[_]: ConcurrentEffect: ContextShift](config: DatabaseConfig, executionContext: ExecutionContext, blocker: Blocker): Resource[F, HikariTransactor[F]] =
    HikariTransactor.newHikariTransactor[F](
      config.driver,
      config.url,
      config.user,
      config.password,
      executionContext,
      blocker
    )

  private def initialize[F[_]: ConcurrentEffect: ContextShift](transactor: HikariTransactor[F]): F[Unit] =
    transactor.configure { dataSource =>
      Sync[F].delay {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }
}
