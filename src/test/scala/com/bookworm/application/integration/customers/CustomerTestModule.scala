package com.bookworm.application.integration.customers

import com.bookworm.application.customers.adapter.publisher.DomainEventPublisher
import com.google.inject.AbstractModule

class CustomerTestModule(domainEventPublisher: DomainEventPublisher) extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[DomainEventPublisher]).toInstance(domainEventPublisher)
}
