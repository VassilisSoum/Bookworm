package com.bookworm.application.customers.domain.port.inbound

import cats.Monad
import cats.implicits._
import com.bookworm.application.customers.domain.model.{CustomerRegistrationStatus, DomainBusinessError}
import com.bookworm.application.customers.domain.port.inbound.command.SaveEmailVerificationTokenCommand
import com.bookworm.application.customers.domain.port.outbound.{CustomerRepository, VerificationTokenRepository}

import javax.inject.Inject

class VerificationTokenUseCase[F[_]: Monad] @Inject() (
    customerRepository: CustomerRepository[F],
    verificationTokenRepository: VerificationTokenRepository[F]
) {

  def saveEmailVerificationToken(
    saveEmailVerificationTokenCommand: SaveEmailVerificationTokenCommand
  ): F[Either[DomainBusinessError, Unit]] =
    customerRepository.findBy(saveEmailVerificationTokenCommand.customerId).flatMap {
      case Some(customer) if customer.customerRegistrationStatus == CustomerRegistrationStatus.Pending =>
        verificationTokenRepository
          .removeAll(saveEmailVerificationTokenCommand.customerId)
          .flatMap(_ =>
            verificationTokenRepository.save(saveEmailVerificationTokenCommand.toDomainObject).map(Right(_))
          )
      case Some(customer) if customer.customerRegistrationStatus == CustomerRegistrationStatus.Completed =>
        Monad[F].pure(Left(DomainBusinessError.CustomerAlreadyRegistered))
      case Some(customer) if customer.customerRegistrationStatus == CustomerRegistrationStatus.Expired =>
        Monad[F].pure(Left(DomainBusinessError.CustomerDoesNotExists))
      case None =>
        Monad[F].pure(Left(DomainBusinessError.CustomerDoesNotExists))
    }
}
