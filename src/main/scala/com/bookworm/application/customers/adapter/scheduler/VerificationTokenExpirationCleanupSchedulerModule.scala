package com.bookworm.application.customers.adapter.scheduler

import com.bookworm.application.config.Configuration.ExpiredVerificationTokensSchedulerConfig
import com.google.inject.AbstractModule

class VerificationTokenExpirationCleanupSchedulerModule(config: ExpiredVerificationTokensSchedulerConfig)
  extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[ExpiredVerificationTokensSchedulerConfig]).toInstance(config)
}
