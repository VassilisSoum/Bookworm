package com.bookworm.application.customers.adapter.service

import cats.effect.IO
import cats.implicits._
import com.bookworm.application.config.Configuration.CustomerConfig
import com.bookworm.application.customers.adapter.logger
import com.bookworm.application.customers.adapter.producer.DomainEventProducer
import com.bookworm.application.customers.adapter.service.model.{CompleteCustomerRegistrationServiceModel, InitiateCustomerRegistrationServiceModel}
import com.bookworm.application.customers.domain.model.{DomainBusinessError, VerificationToken}
import com.bookworm.application.customers.domain.port.inbound.command.{CompleteCustomerRegistrationCommand, InitiateCustomerRegistrationCommand, SaveEmailVerificationTokenCommand}
import com.bookworm.application.customers.domain.port.inbound.query.CustomerQueryModel
import com.bookworm.application.customers.domain.port.inbound.{RegisterCustomerUseCase, VerificationTokenUseCase}
import com.bookworm.application.customers.domain.port.outbound.event.{CompleteCustomerRegistrationFinishedEvent, DomainEventPublicationStatus, InitialCustomerRegistrationPendingEvent}
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import java.time.{Clock, LocalDateTime}
import java.util.UUID
import javax.inject.Inject

class CustomerApplicationService @Inject() (
    registerCustomerUseCase: RegisterCustomerUseCase[ConnectionIO],
    verificationTokenUseCase: VerificationTokenUseCase[ConnectionIO],
    domainEventProducer: DomainEventProducer,
    customerApplicationConfig: CustomerConfig,
    transactor: Transactor[IO],
    clock: Clock
) {

  def initiateCustomerRegistration(
    initiateCustomerRegistrationServiceModel: InitiateCustomerRegistrationServiceModel
  ): IO[Either[DomainBusinessError, Unit]] = {
    val initiateCustomerRegistrationCommand = InitiateCustomerRegistrationCommand(
      initiateCustomerRegistrationServiceModel.id,
      initiateCustomerRegistrationServiceModel.firstName,
      initiateCustomerRegistrationServiceModel.lastName,
      initiateCustomerRegistrationServiceModel.email,
      initiateCustomerRegistrationServiceModel.age,
      initiateCustomerRegistrationServiceModel.password
    )

    val verificationTokenExpiration =
      LocalDateTime.now(clock).plusSeconds(customerApplicationConfig.verificationTokenExpirationInSeconds)
    val saveEmailVerificationTokenCommand = SaveEmailVerificationTokenCommand(
      VerificationToken(UUID.randomUUID()),
      initiateCustomerRegistrationCommand.id,
      verificationTokenExpiration
    )
    val initiateRegistrationResponse: ConnectionIO[Either[DomainBusinessError, Unit]] = registerCustomerUseCase
      .initiateRegistration(initiateCustomerRegistrationCommand)

    initiateRegistrationResponse
      .flatMap {
        case Left(_)  => initiateRegistrationResponse
        case Right(_) => verificationTokenUseCase.saveEmailVerificationToken(saveEmailVerificationTokenCommand)
      }
      .transact(transactor)
      .flatTap(resultE =>
        IO.pure(
          logger.info(
            s"Result of initiating registration for customer with id ${initiateCustomerRegistrationCommand.id.id} is $resultE"
          )
        )
      )
      .flatMap { resultE =>
        if (resultE.isRight) {
          domainEventProducer
            .produce(
              InitialCustomerRegistrationPendingEvent(
                id = UUID.randomUUID(),
                customerId = initiateCustomerRegistrationCommand.id.id,
                creationDate = LocalDateTime.now(clock),
                verificationToken = saveEmailVerificationTokenCommand.token
              )
            )
            .flatTap {
              case DomainEventPublicationStatus.Published =>
                IO.pure(logger.info("Published initial customer registration event"))
              case DomainEventPublicationStatus.NotPublished =>
                IO.pure(logger.error("Could not publish initial customer registration event"))
            }
        }
        IO.pure(resultE)
      }
  }

  def completeCustomerRegistration(
    completeCustomerRegistrationServiceModel: CompleteCustomerRegistrationServiceModel
  ): IO[Either[DomainBusinessError, CustomerQueryModel]] = {
    val completeCustomerRegistrationCommand = CompleteCustomerRegistrationCommand(
      completeCustomerRegistrationServiceModel.verificationToken
    )
    registerCustomerUseCase
      .completeCustomerRegistration(completeCustomerRegistrationCommand)
      .transact(transactor)
      .flatTap {
        case Left(domainBusinessError) =>
          IO.pure(logger.error(s"Failed to complete customer registration with error $domainBusinessError"))
        case Right(customer) =>
          IO.pure(
            logger.info(s"Successfully completed customer registration for customer id ${customer.customerId}")
          )
      }
      .flatMap { resultE =>
        resultE match {
          case Right(customer) =>
            domainEventProducer
              .produce(
                CompleteCustomerRegistrationFinishedEvent(
                  UUID.randomUUID(),
                  customer.customerId,
                  LocalDateTime.now(clock)
                )
              )
              .flatTap {
                case DomainEventPublicationStatus.Published =>
                  IO.pure(logger.info("Published initial customer registration event"))
                case DomainEventPublicationStatus.NotPublished =>
                  IO.pure(logger.error("Could not publish initial customer registration event"))
              }
              .flatMap(_ => IO.pure(resultE))
          case Left(_) =>
            IO.pure(resultE)
        }
      }
  }
}
