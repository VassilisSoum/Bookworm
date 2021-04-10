package com.bookworm.application.customers.adapter.scheduler

import com.bookworm.application.config.Configuration.ExpiredVerificationTokensSchedulerConfig
import com.bookworm.application.customers.adapter.logger
import com.bookworm.application.customers.adapter.service.CustomerVerificationTokenApplicationService
import com.google.common.util.concurrent.AbstractScheduledService
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler

import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VerificationTokenExpirationCleanupScheduler @Inject() (
    customerVerificationTokenApplicationService: CustomerVerificationTokenApplicationService,
    expiredVerificationTokensSchedulerConfig: ExpiredVerificationTokensSchedulerConfig
) extends AbstractScheduledService {

  override def startUp(): Unit = {
    logger.info("Starting clean up scheduler for expired email verification tokens")
    super.startUp()
  }

  override def runOneIteration(): Unit =
    customerVerificationTokenApplicationService
      .removeExpiredVerificationTokens()
      .unsafeRunSync()
      .fold(
        exception => {
          logger.error(s"Clean up iteration of expired verification tokens failed with $exception")
          ()
        },
        _ => ()
      )

  override def scheduler(): AbstractScheduledService.Scheduler =
    Scheduler.newFixedRateSchedule(
      0L,
      expiredVerificationTokensSchedulerConfig.periodInMillis,
      TimeUnit.MILLISECONDS
    )
}
