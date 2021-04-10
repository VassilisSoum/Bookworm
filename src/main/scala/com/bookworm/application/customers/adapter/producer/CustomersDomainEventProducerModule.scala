package com.bookworm.application.customers.adapter.producer

import com.google.inject.{AbstractModule, Scopes}

class CustomersDomainEventProducerModule extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[DomainEventProducer]).in(Scopes.SINGLETON)
}
