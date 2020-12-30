package com.bookworm.application.init

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, Resource, Timer}
import com.bookworm.application.config.Configuration
import com.bookworm.application.config.Configuration.{Config, DatabaseConfig}
import com.bookworm.application.books.repository.RepositoryModule
import com.bookworm.application.books.rest.{BookRestApi, RestModule}
import com.bookworm.application.books.service.ServiceModule
import com.google.inject.{AbstractModule, Guice, TypeLiteral}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import net.codingwell.scalaguice.ScalaModule
import org.flywaydb.core.Flyway
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

object BookwormServer {

  def stream(implicit T: Timer[IO], CS: ContextShift[IO]): IO[ExitCode] =
    resources.use(create)

  private def resources(implicit contextShift: ContextShift[IO]): Resource[IO, (HikariTransactor[IO], Config)] =
    for {
      config <- Configuration.load()
      ec <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      blocker <- Blocker[IO]
      transactor <- transactor(config.database, ec, blocker)
    } yield (transactor, config)

  private def create(
    resources: (HikariTransactor[IO], Config)
  )(implicit concurrentEffect: ConcurrentEffect[IO], timer: Timer[IO]): IO[ExitCode] = {
    val injector = Guice.createInjector(
      new RestModule(),
      new ServiceModule(),
      new RepositoryModule(),
      new AbstractModule with ScalaModule {
        override def configure(): Unit =
          bind(new TypeLiteral[HikariTransactor[IO]] {}).toInstance(resources._1)
      }
    )
    for {
      _ <- initialize(resources._1)
      httpApp = Logger.httpApp(logHeaders = true, logBody = true)(
        injector.getInstance(classOf[BookRestApi]).getAllBooks /*<+>*/ .orNotFound
      )
      exitCode <- BlazeServerBuilder[IO](global)
        .bindHttp(resources._2.server.port, resources._2.server.host)
        .withHttpApp(httpApp)
        .serve
        .compile
        .lastOrError
    } yield {
      Guice.createInjector()
      exitCode
    }
  }

  private def transactor(config: DatabaseConfig, executionContext: ExecutionContext, blocker: Blocker)(implicit
    contextShift: ContextShift[IO]
  ): Resource[IO, HikariTransactor[IO]] =
    HikariTransactor.newHikariTransactor[IO](
      config.driver,
      config.url,
      config.user,
      config.password,
      executionContext,
      blocker
    )

  private def initialize(transactor: HikariTransactor[IO]): IO[Unit] =
    transactor.configure { dataSource =>
      IO {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }
}
