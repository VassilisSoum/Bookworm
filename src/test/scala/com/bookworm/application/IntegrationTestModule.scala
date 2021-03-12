package com.bookworm.application

import cats.effect.{Blocker, ContextShift, IO}
import cats.{Monad, MonadError}
import com.bookworm.application.books.adapter.api.BookRestApi
import com.bookworm.application.books.adapter.repository.BookRepositoryModule
import com.bookworm.application.books.adapter.repository.dao.BookDao
import com.bookworm.application.books.adapter.service.BookApplicationService
import com.bookworm.application.books.domain.port.inbound.{AddBookUseCase, GetBooksByGenreUseCase, RemoveBookUseCase, UpdateBookUseCase}
import com.bookworm.application.integration.FakeClock
import com.dimafeng.testcontainers.{Container, DockerComposeContainer, ExposedService, ForAllTestContainer}
import com.google.inject._
import doobie.util.transactor.Transactor
import doobie.{ConnectionIO, ExecutionContexts}
import net.codingwell.scalaguice.ScalaModule
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpec}

import java.io.File
import java.time.LocalDateTime

abstract class IntegrationTestModule
  extends WordSpec
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with ForAllTestContainer {

  implicit private val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  private val databaseName: String = "Bookworm"
  private val jdbcUrl: String = s"jdbc:postgresql://localhost:5432/$databaseName"
  private val username: String = "Bookworm"
  private val password: String = "password"

  val fakeClock: FakeClock = new FakeClock

  override val container: Container =
    DockerComposeContainer(new File("docker-compose.yml"), exposedServices = Seq(ExposedService("db", 5432)))

  lazy val synchronousTransactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    jdbcUrl,
    username,
    password,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  lazy val injector: Injector = Guice.createInjector(
    new AbstractModule with ScalaModule {

      override def configure(): Unit = {
        bind(new TypeLiteral[Monad[ConnectionIO]] {}).toInstance(implicitly[Monad[ConnectionIO]])
        bind(new TypeLiteral[MonadError[ConnectionIO, Throwable]] {})
          .toInstance(implicitly[MonadError[ConnectionIO, Throwable]])
        bind(classOf[BookDao]).in(Scopes.SINGLETON)
        bind(classOf[BookApplicationService]).in(Scopes.SINGLETON)
        bind(new TypeLiteral[GetBooksByGenreUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
        bind(new TypeLiteral[AddBookUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
        bind(new TypeLiteral[RemoveBookUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
        bind(new TypeLiteral[UpdateBookUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
        bind(new TypeLiteral[Transactor[IO]] {}).toInstance(synchronousTransactor)
        bind(new TypeLiteral[BookRestApi] {}).in(Scopes.SINGLETON)
        bind(classOf[java.time.Clock]).toInstance(fakeClock)
      }
    },
    new BookRepositoryModule
  )

  override def beforeAll(): Unit = {
    fakeClock.current = LocalDateTime
      .of(2025, 2, 7, 10, 0, 0)
      .atZone(fakeClock.zoneId)
      .toInstant
    super.beforeAll()
  }

  override def afterStart(): Unit =
    synchronousTransactor
      .configure { _ =>
        IO {
          val flyWay = Flyway
            .configure()
            .dataSource(jdbcUrl, username, password)
            .load()
          flyWay.migrate()
          ()
        }
      }
      .unsafeRunSync()
}
