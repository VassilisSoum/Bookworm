package com.bookworm.application

import cats.effect.{Blocker, ContextShift, IO}
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.bookworm.application.Main.Module
import com.bookworm.application.books.adapter.api.BooksRestApiModule
import com.bookworm.application.books.adapter.repository.BookRepositoryModule
import com.bookworm.application.books.adapter.repository.dao.BooksDaoModule
import com.bookworm.application.books.adapter.service.BooksApplicationServiceModule
import com.bookworm.application.config.Configuration.{AwsConfig, CustomerConfig, CustomerRegistrationVerificationConfig, ExpiredVerificationTokensSchedulerConfig}
import com.bookworm.application.config.module.{BooksUseCasesModule, CustomersUseCasesModule}
import com.bookworm.application.customers.adapter.api.CustomersRestApiModule
import com.bookworm.application.customers.adapter.publisher.DomainEventPublisher
import com.bookworm.application.customers.adapter.repository.CustomerRepositoryModule
import com.bookworm.application.customers.adapter.repository.dao.CustomersDaoModule
import com.bookworm.application.customers.adapter.scheduler.VerificationTokenExpirationCleanupSchedulerModule
import com.bookworm.application.customers.adapter.service.CustomersApplicationServiceModule
import com.bookworm.application.integration.FakeClock
import com.bookworm.application.integration.customers.{CustomerTestModule, FakeDomainEventPublisher}
import com.dimafeng.testcontainers.{Container, DockerComposeContainer, ExposedService, ForAllTestContainer}
import com.google.inject._
import doobie.ExecutionContexts
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpec}

import java.io.File
import java.time.LocalDateTime

abstract class IntegrationTestModule
  extends WordSpec
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MockFactory
  with ForAllTestContainer {

  implicit private val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  private val databaseName: String = "Bookworm"
  private val jdbcUrl: String = s"jdbc:postgresql://localhost:5432/$databaseName"
  private val username: String = "Bookworm"
  private val password: String = "password"

  val customerRegistrationVerificationConfig: CustomerRegistrationVerificationConfig =
    CustomerRegistrationVerificationConfig(
      "senderEmail@test.com",
      "verification-email-template",
      32
    )
  private val customerConfig: CustomerConfig = CustomerConfig(86400, customerRegistrationVerificationConfig)
  private val awsConfig: AwsConfig = AwsConfig("us-east-2", "bookworm-ses-set")

  private val expiredVerificationTokensSchedulerConfig: ExpiredVerificationTokensSchedulerConfig =
    ExpiredVerificationTokensSchedulerConfig(enabled = false, periodInMillis = 1000L)

  val fakeClock: FakeClock = new FakeClock

  val customerDomainEventPublisher: DomainEventPublisher = new FakeDomainEventPublisher(
    customerRegistrationVerificationEmailProducerService = null
  )

  val amazonSimpleEmailService: AmazonSimpleEmailService = mock[AmazonSimpleEmailService]

  override val container: Container =
    DockerComposeContainer(new File("docker-compose.yml"), exposedServices = Seq(ExposedService("db", 5432)))

  lazy val synchronousTransactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    jdbcUrl,
    username,
    password,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  lazy val injector: Injector = wireDependencies(customerConfig, expiredVerificationTokensSchedulerConfig)

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

  private def wireDependencies(
    customerConfig: CustomerConfig,
    expiredVerificationTokensSchedulerConfig: ExpiredVerificationTokensSchedulerConfig
  ): Injector =
    Guice.createInjector(
      new Module(synchronousTransactor, fakeClock),
      new BookRepositoryModule,
      new BooksRestApiModule,
      new CustomersRestApiModule,
      new BooksDaoModule,
      new BooksApplicationServiceModule,
      new BooksUseCasesModule,
      new CustomersUseCasesModule,
      new CustomerRepositoryModule,
      new CustomerTestModule(customerDomainEventPublisher),
      new CustomersApplicationServiceModule(
        customerConfig = customerConfig,
        awsConfig = awsConfig,
        amazonSimpleEmailService = amazonSimpleEmailService,
        executionContext = ExecutionContexts.synchronous
      ),
      new CustomersDaoModule,
      new VerificationTokenExpirationCleanupSchedulerModule(expiredVerificationTokensSchedulerConfig)
    )
}
