package com.bookworm.application.integration

import cats.effect.{Blocker, ContextShift, IO}
import com.bookworm.application.repository.RepositoryModule
import com.bookworm.application.rest.RestModule
import com.bookworm.application.service.ServiceModule
import com.dimafeng.testcontainers.{Container, DockerComposeContainer, ExposedService, ForAllTestContainer}
import com.google.inject.{AbstractModule, Guice, Injector, TypeLiteral}
import doobie.ExecutionContexts
import doobie.util.transactor.Transactor
import net.codingwell.scalaguice.ScalaModule
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import java.io.File

abstract class IntegrationTestModule extends WordSpec with Matchers with ForAllTestContainer with BeforeAndAfterAll {

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

  lazy val injector: Injector = Guice.createInjector(
    new RestModule(),
    new ServiceModule(),
    new RepositoryModule(),
    new AbstractModule with ScalaModule {

      override def configure(): Unit =
        bind(new TypeLiteral[Transactor[IO]] {}).toInstance(synchronousTransactor)
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
