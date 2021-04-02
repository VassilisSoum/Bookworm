package com.bookworm.application.customers.adapter.service

import cats.effect.IO
import cats.implicits._
import com.bookworm.application.customers.adapter.logger
import com.bookworm.application.customers.domain.model.DomainBusinessError
import com.bookworm.application.customers.domain.port.inbound.VerificationTokenUseCase
import com.bookworm.application.customers.domain.port.inbound.command.SaveEmailVerificationTokenCommand
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import javax.inject.Inject

class VerificationTokenApplicationService @Inject() (
    verificationTokenUseCase: VerificationTokenUseCase[ConnectionIO],
    transactor: Transactor[IO]
) {

  def saveEmailVerificationToken(
    saveEmailVerificationTokenCommand: SaveEmailVerificationTokenCommand
  ): IO[Either[DomainBusinessError, Unit]] =
    verificationTokenUseCase
      .saveEmailVerificationToken(saveEmailVerificationTokenCommand)
      .transact(transactor)
      .flatTap {
        case Left(domainBusinessError) =>
          IO.pure(
            logger.error(
              s"Could not save email verification for customer with id ${saveEmailVerificationTokenCommand.customerId.id} with error $domainBusinessError"
            )
          )
        case Right(_) =>
          IO.pure(
            logger.info(
              s"Saved email verification token for customer with id ${saveEmailVerificationTokenCommand.customerId.id}"
            )
          )
      }
}
