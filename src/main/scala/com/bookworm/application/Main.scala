package com.bookworm.application

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.semigroupk._
import cats.{Monad, MonadError}
import com.bookworm.application.books.adapter.api.{AuthorRestApi, BookRestApi, BooksRestApiModule}
import com.bookworm.application.books.adapter.repository.BookRepositoryModule
import com.bookworm.application.books.adapter.repository.dao.BooksDaoModule
import com.bookworm.application.books.adapter.service.BooksApplicationServiceModule
import com.bookworm.application.config.module.{BooksUseCasesModule, CustomersUseCasesModule}
import com.bookworm.application.customers.adapter.api.{CustomerRegistrationRestApi, CustomersRestApiModule}
import com.bookworm.application.customers.adapter.producer.CustomersDomainEventProducerModule
import com.bookworm.application.customers.adapter.repository.CustomerRepositoryModule
import com.bookworm.application.customers.adapter.repository.dao.CustomersDaoModule
import com.bookworm.application.customers.adapter.scheduler.{VerificationTokenExpirationCleanupScheduler, VerificationTokenExpirationCleanupSchedulerModule}
import com.bookworm.application.customers.adapter.service.CustomersApplicationServiceModule
import com.bookworm.application.init.BookwormServer
import com.google.common.util.concurrent.{AbstractScheduledService, Service}
import com.google.inject._
import doobie.{ConnectionIO, Transactor}
import net.codingwell.scalaguice.ScalaModule
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import java.time.Clock
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    BookwormServer.createTransactor
      .use { resources =>
        val injector = Guice.createInjector(
          new Module(resources._1, java.time.Clock.systemUTC()),
          new BookRepositoryModule,
          new BooksRestApiModule,
          new CustomersRestApiModule,
          new BooksDaoModule,
          new BooksApplicationServiceModule,
          new BooksUseCasesModule,
          new CustomersUseCasesModule,
          new CustomerRepositoryModule,
          new CustomersDomainEventProducerModule,
          new CustomersApplicationServiceModule(resources._2.customer),
          new CustomersDaoModule,
          new VerificationTokenExpirationCleanupSchedulerModule(resources._2.expiredVerificationTokensScheduler)
        )
        val schedulers = ListBuffer[AbstractScheduledService]()
        if (resources._2.expiredVerificationTokensScheduler.enabled) {
          schedulers += injector.getInstance(classOf[VerificationTokenExpirationCleanupScheduler])
        }
        val httpApp = Logger.httpApp(logHeaders = true, logBody = true)(
          Router(
            "/" -> injector
              .getInstance(classOf[BookRestApi])
              .routes
              .<+>(injector.getInstance(classOf[AuthorRestApi]).routes)
              .<+>(injector.getInstance(classOf[CustomerRegistrationRestApi]).routes)
          ).orNotFound
        )
        for {
          _ <- BookwormServer.migrate(resources._1)
          _ = startSchedulers(schedulers).unsafeRunSync()
          exitCode <- BlazeServerBuilder[IO](global)
            .bindHttp(resources._2.server.port, resources._2.server.host)
            .withHttpApp(httpApp)
            .serve
            .compile
            .lastOrError
        } yield exitCode
      }

  class Module(transactor: Transactor[IO], clock: Clock) extends AbstractModule with ScalaModule {

    import cats.effect._

    override def configure(): Unit = {
      bind(new TypeLiteral[Monad[ConnectionIO]] {}).toInstance(implicitly[Monad[ConnectionIO]])
      bind(new TypeLiteral[MonadError[ConnectionIO, Throwable]] {})
        .toInstance(implicitly[MonadError[ConnectionIO, Throwable]])
      bind(new TypeLiteral[Transactor[IO]] {}).toInstance(transactor)
      bind(classOf[java.time.Clock]).toInstance(clock)
    }
  }

  def startSchedulers(schedulers: ListBuffer[AbstractScheduledService]): IO[mutable.Seq[Service]] =
    IO(schedulers.map(scheduler => scheduler.startAsync()))
}
