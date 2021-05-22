package com.bookworm.application.customers.adapter.service

import cats.effect.IO
import cats.implicits._
import com.bookworm.application.config.Configuration.CustomerConfig
import com.bookworm.application.customers.adapter.logger
import com.bookworm.application.customers.adapter.publisher.DomainEventPublisher
import com.bookworm.application.customers.adapter.service.model.{CustomerCompetionRegistrationServiceModel, CustomerInitiationRegistrationServiceModel}
import com.bookworm.application.customers.domain.model.event.{CompleteCustomerRegistrationFinishedEvent, DomainEventPublicationStatus, InitialCustomerRegistrationPendingEvent}
import com.bookworm.application.customers.domain.model.{CustomerId, DomainBusinessError, VerificationToken}
import com.bookworm.application.customers.domain.port.inbound.command.{CompleteCustomerRegistrationCommand, InitiateCustomerRegistrationCommand, SaveEmailVerificationTokenCommand}
import com.bookworm.application.customers.domain.port.inbound.query.CustomerQueryModel
import com.bookworm.application.customers.domain.port.inbound.{RegisterCustomerUseCase, RetrieveCustomerDetailsUseCase, VerificationTokenUseCase}
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import java.time.{Clock, LocalDateTime}
import java.util.UUID
import javax.inject.Inject

class CustomerRegistrationApplicationService @Inject()(
    registerCustomerUseCase: RegisterCustomerUseCase[ConnectionIO],
    verificationTokenUseCase: VerificationTokenUseCase[ConnectionIO],
    retrieveCustomerDetailsUseCase: RetrieveCustomerDetailsUseCase[ConnectionIO],
    domainEventProducer: DomainEventPublisher,
    customerApplicationConfig: CustomerConfig,
    transactor: Transactor[IO],
    clock: Clock
) {

  def initiateCustomerRegistration(
    initiateCustomerRegistrationServiceModel: CustomerInitiationRegistrationServiceModel
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
        IO.delay(
          logger.info(
            s"Result of initiating registration for customer with id ${initiateCustomerRegistrationCommand.id.id} is $resultE"
          )
        )
      )
      .flatMap { resultE =>
        if (resultE.isRight) {
          domainEventProducer
            .publish(
              InitialCustomerRegistrationPendingEvent(
                id = UUID.randomUUID(),
                customerId = initiateCustomerRegistrationCommand.id.id,
                creationDate = LocalDateTime.now(clock),
                verificationToken = saveEmailVerificationTokenCommand.token,
                customerFirstName = initiateCustomerRegistrationCommand.firstName.value,
                customerLastName = initiateCustomerRegistrationCommand.lastName.value,
                customerEmail = initiateCustomerRegistrationCommand.email.value
              )
            )
            .flatTap {
              case DomainEventPublicationStatus.Published =>
                IO.delay(logger.info("Published initial customer registration event"))
              case DomainEventPublicationStatus.NotPublished =>
                IO.delay(logger.error("Could not publish initial customer registration event"))
            }
            .map(_ => Right(()))
        } else {
          IO.pure(resultE)
        }
      }
  }

  def completeCustomerRegistration(
    completeCustomerRegistrationServiceModel: CustomerCompetionRegistrationServiceModel
  ): IO[Either[DomainBusinessError, CustomerQueryModel]] = {
    val completeCustomerRegistrationCommand = CompleteCustomerRegistrationCommand(
      completeCustomerRegistrationServiceModel.verificationToken
    )
    registerCustomerUseCase
      .completeCustomerRegistration(completeCustomerRegistrationCommand)
      .transact(transactor)
      .flatTap {
        case Left(domainBusinessError) =>
          IO.delay(logger.error(s"Failed to complete customer registration with error $domainBusinessError"))
        case Right(customer) =>
          IO.delay(
            logger.info(s"Successfully completed customer registration for customer id ${customer.customerId}")
          )
      }
      .flatMap { resultE =>
        resultE match {
          case Right(customer) =>
            domainEventProducer
              .publish(
                CompleteCustomerRegistrationFinishedEvent(
                  UUID.randomUUID(),
                  customer.customerId,
                  LocalDateTime.now(clock)
                )
              )
              .flatTap {
                case DomainEventPublicationStatus.Published =>
                  IO.delay(logger.info("Published initial customer registration event"))
                case DomainEventPublicationStatus.NotPublished =>
                  IO.delay(logger.error("Could not publish initial customer registration event"))
              }
              .map(_ => resultE)
          case Left(_) =>
            IO.pure(resultE)
        }
      }
  }

  def retrieveCustomerDetails(customerId: CustomerId): IO[Option[CustomerQueryModel]] =
    retrieveCustomerDetailsUseCase
      .retrieveCustomerDetails(customerId)
      .transact(transactor)
}
