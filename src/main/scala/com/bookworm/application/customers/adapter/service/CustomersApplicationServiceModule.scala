package com.bookworm.application.customers.adapter.service

import com.bookworm.application.config.Configuration.CustomerConfig
import com.google.inject.{AbstractModule, Scopes}

class CustomersApplicationServiceModule(customerConfig: CustomerConfig) extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[CustomerApplicationService]).in(Scopes.SINGLETON)
    bind(classOf[CustomerVerificationTokenApplicationService]).in(Scopes.SINGLETON)
    bind(classOf[CustomerConfig]).toInstance(customerConfig)
  }
}
