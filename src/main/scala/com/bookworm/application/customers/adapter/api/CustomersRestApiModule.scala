package com.bookworm.application.customers.adapter.api

import com.google.inject.{AbstractModule, Scopes}

class CustomersRestApiModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[CustomerRegistrationRestApi]).in(Scopes.SINGLETON)
  }
}
