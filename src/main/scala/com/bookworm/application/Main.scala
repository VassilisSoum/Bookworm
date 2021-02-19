package com.bookworm.application

import cats.effect.{ExitCode, IO, IOApp}
import com.bookworm.application.books.adapter.api.BookRestApi
import com.bookworm.application.books.adapter.repository.BookRepositoryModule
import com.bookworm.application.books.adapter.repository.dao.BookDao
import com.bookworm.application.books.domain.port.inbound.BookService
import com.bookworm.application.init.BookwormServer
import com.google.inject._
import doobie.Transactor
import doobie.hikari.HikariTransactor
import net.codingwell.scalaguice.ScalaModule
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    BookwormServer.createTransactor
      .use { resources =>
        val injector = Guice.createInjector(
          new Module(resources._1),
          new BookRepositoryModule
        )
        val httpApp = Logger.httpApp(logHeaders = true, logBody = true)(
          Router("/" -> injector.getInstance(classOf[BookRestApi]).routes).orNotFound
        )
        for {
          _ <- BookwormServer.migrate(resources._1)
          exitCode <- BlazeServerBuilder[IO](global)
            .bindHttp(resources._2.server.port, resources._2.server.host)
            .withHttpApp(httpApp)
            .serve
            .compile
            .lastOrError
        } yield exitCode
      }

  class Module(transactor: HikariTransactor[IO]) extends AbstractModule with ScalaModule {

    import cats.effect._

    override def configure(): Unit = {
      //bind(new TypeLiteral[Sync[IO]] {}).toInstance(implicitly[Sync[IO]])
      bind(classOf[BookDao]).in(Scopes.SINGLETON)
      bind(classOf[BookService]).in(Scopes.SINGLETON)
      bind(new TypeLiteral[Transactor[IO]] {}).toInstance(transactor)
      bind(classOf[BookRestApi]).in(Scopes.SINGLETON)
      bind(classOf[java.time.Clock]).toInstance(java.time.Clock.systemUTC())
    }
  }
}
