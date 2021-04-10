package com.bookworm.application.customers.domain.port.inbound

import cats.Monad
import cats.implicits._
import com.bookworm.application.customers.domain.model.{CustomerId, CustomerRegistrationStatus, DomainBusinessError}
import com.bookworm.application.customers.domain.port.inbound.command.{CompleteCustomerRegistrationCommand, InitiateCustomerRegistrationCommand}
import com.bookworm.application.customers.domain.port.inbound.query.CustomerQueryModel
import com.bookworm.application.customers.domain.port.outbound.{CustomerRepository, VerificationTokenRepository}

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject

class RegisterCustomerUseCase[F[_]: Monad] @Inject() (
    customerRepository: CustomerRepository[F],
    verificationTokenRepository: VerificationTokenRepository[F],
    clock: Clock
) {

  def initiateRegistration(
    initiateCustomerRegistrationCommand: InitiateCustomerRegistrationCommand
  ): F[Either[DomainBusinessError, Unit]] =
    customerRepository
      .exists(initiateCustomerRegistrationCommand.email)
      .ifM(
        Monad[F].pure(Left(DomainBusinessError.CustomerAlreadyExists)),
        customerRepository.save(initiateCustomerRegistrationCommand.toDomainObject).map(Right(_))
      )

  def completeCustomerRegistration(
    completeCustomerRegistrationCommand: CompleteCustomerRegistrationCommand
  ): F[Either[DomainBusinessError, CustomerQueryModel]] =
    verificationTokenRepository.findBy(completeCustomerRegistrationCommand.verificationToken).flatMap {
      case Some(customerVerificationToken) =>
        if (LocalDateTime.now(clock).isAfter(customerVerificationToken.expirationDate)) {
          Monad[F].pure(Left(DomainBusinessError.VerificationTokenExpired))
        } else {
          customerRepository.findBy(customerVerificationToken.customerId).flatMap {
            case Some(customerQueryModel)
                if customerQueryModel.customerRegistrationStatus == CustomerRegistrationStatus.Pending =>
              customerRepository
                .updateRegistrationStatus(
                  CustomerId(customerQueryModel.customerId),
                  CustomerRegistrationStatus.Completed
                )
                .map(_ =>
                  Right(customerQueryModel.copy(customerRegistrationStatus = CustomerRegistrationStatus.Completed))
                )
            case _ =>
              Monad[F].pure(Left(DomainBusinessError.CustomerDoesNotExists))
          }
        }
      case None =>
        Monad[F].pure(Left(DomainBusinessError.VerificationTokenDoesNotExists))
    }
}
