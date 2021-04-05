package com.bookworm.application.customers.adapter.service

import cats.effect.IO
import cats.implicits._
import com.bookworm.application.config.Configuration.CustomerConfig
import com.bookworm.application.customers.adapter.logger
import com.bookworm.application.customers.adapter.service.model.SaveEmailVerificationTokenServiceModel
import com.bookworm.application.customers.domain.model.DomainBusinessError
import com.bookworm.application.customers.domain.port.inbound.VerificationTokenUseCase
import com.bookworm.application.customers.domain.port.inbound.command.SaveEmailVerificationTokenCommand
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject

class VerificationTokenApplicationService @Inject() (
    verificationTokenUseCase: VerificationTokenUseCase[ConnectionIO],
    transactor: Transactor[IO],
    customerApplicationConfig: CustomerConfig,
    clock: Clock
) {

  def saveEmailVerificationToken(
    saveEmailVerificationTokenServiceModel: SaveEmailVerificationTokenServiceModel
  ): IO[Either[DomainBusinessError, Unit]] = {
    val saveEmailVerificationTokenCommand = SaveEmailVerificationTokenCommand(
      token = saveEmailVerificationTokenServiceModel.verificationToken,
      customerId = saveEmailVerificationTokenServiceModel.customerId,
      /*
       * The only issue is that current time does not reflect the actual time the email was sent.
       *  But not so much a real problem
       */
      expirationDate =
        LocalDateTime.now(clock).plusSeconds(customerApplicationConfig.verificationTokenExpirationInSeconds)
    )
    verificationTokenUseCase
      .saveEmailVerificationToken(saveEmailVerificationTokenCommand)
      .transact(transactor)
      .flatTap {
        case Left(domainBusinessError) =>
          IO.pure(
            logger.error(
              s"Could not save email verification for customer with id ${saveEmailVerificationTokenCommand.customerId.id} " +
              s"with error $domainBusinessError"
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
}
