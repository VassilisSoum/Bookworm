package com.bookworm.application.customers.adapter.service

import cats.effect.IO
import cats.implicits._
import com.bookworm.application.customers.adapter.logger
import com.bookworm.application.customers.domain.port.inbound.VerificationTokenUseCase
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class CustomerVerificationTokenApplicationService @Inject() (
    verificationTokenUseCase: VerificationTokenUseCase[ConnectionIO],
    transactor: Transactor[IO]
) {

  def removeExpiredVerificationTokens(): IO[Try[Unit]] =
    verificationTokenUseCase
      .removeExpiredVerificationTokens()
      .transact(transactor)
      .attempt
      .flatTap {
        case Left(failure) =>
          IO.pure(logger.error(s"Could not remove expired verification tokens. $failure"))
        case Right(_) =>
          IO.pure(logger.info("Removed any expired verification tokens"))
      }
      .map {
        case Left(exception) => Failure(exception)
        case Right(_)        => Success(())
      }
}
