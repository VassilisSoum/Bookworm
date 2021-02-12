package com.bookworm.application

import cats.effect.{Blocker, ContextShift, IO, Sync}
import com.bookworm.application.books.adapter.api.BookRestApi
import com.bookworm.application.books.adapter.repository.BookRepositoryImpl
import com.bookworm.application.books.adapter.repository.dao.BookDao
import com.bookworm.application.books.adapter.service.BookServiceImpl
import com.bookworm.application.books.domain.port.inbound.BookService
import com.bookworm.application.books.domain.port.outbound.BookRepository
import com.bookworm.application.integration.FakeClock
import com.dimafeng.testcontainers.{Container, DockerComposeContainer, ExposedService, ForAllTestContainer}
import com.google.inject._
import doobie.ExecutionContexts
import doobie.util.transactor.Transactor
import net.codingwell.scalaguice.ScalaModule
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpec}

import java.io.File

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

  override val container: Container =
    DockerComposeContainer(new File("docker-compose.yml"), exposedServices = Seq(ExposedService("db", 5432)))

  lazy val synchronousTransactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    jdbcUrl,
    username,
    password,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  val fakeClock: FakeClock = new FakeClock

  lazy val injector: Injector = Guice.createInjector(
    new AbstractModule with ScalaModule {

      override def configure(): Unit = {
        bind(new TypeLiteral[Sync[IO]] {}).toInstance(implicitly[Sync[IO]])
        bind(classOf[BookDao]).in(Scopes.SINGLETON)
        bind(new TypeLiteral[Transactor[IO]] {}).toInstance(synchronousTransactor)
        bind(new TypeLiteral[BookRepository[IO]]() {})
          .to(new TypeLiteral[BookRepositoryImpl[IO]]() {})
          .in(Scopes.SINGLETON)
        bind(new TypeLiteral[BookService[IO]]() {})
          .to(new TypeLiteral[BookServiceImpl[IO]]() {})
          .in(Scopes.SINGLETON)
        bind(new TypeLiteral[BookRestApi[IO]] {}).in(Scopes.SINGLETON)
        bind(classOf[java.time.Clock]).toInstance(fakeClock)
      }
    }
  )

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
