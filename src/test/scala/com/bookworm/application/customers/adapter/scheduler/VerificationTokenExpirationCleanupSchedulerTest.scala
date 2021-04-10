package com.bookworm.application.customers.adapter.scheduler

import cats.effect.IO
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.config.Configuration.ExpiredVerificationTokensSchedulerConfig
import com.bookworm.application.customers.adapter.service.CustomerVerificationTokenApplicationService

import scala.util.{Failure, Success}

class VerificationTokenExpirationCleanupSchedulerTest extends AbstractUnitTest {

  val customerVerificationTokenApplicationService = mock[CustomerVerificationTokenApplicationService]

  val expiredVerificationTokenSchedulerConfig = ExpiredVerificationTokensSchedulerConfig(
    enabled = true,
    periodInMillis = 1000L
  )

  val verificationTokenExpirationCleanupScheduler = new VerificationTokenExpirationCleanupScheduler(
    customerVerificationTokenApplicationService,
    expiredVerificationTokenSchedulerConfig
  )

  "VerificationTokenExpirationCleanupScheduler" should {
    "remove expired verification tokens" in {
      (customerVerificationTokenApplicationService.removeExpiredVerificationTokens _)
        .expects()
        .returns(IO.pure(Success(())))
        .once()
      verificationTokenExpirationCleanupScheduler.runOneIteration()
    }

    "not throw exception in case of error" in {
      (customerVerificationTokenApplicationService.removeExpiredVerificationTokens _)
        .expects()
        .returns(IO.pure(Failure(new Exception())))
        .once()
      verificationTokenExpirationCleanupScheduler.runOneIteration()
    }
  }
}
