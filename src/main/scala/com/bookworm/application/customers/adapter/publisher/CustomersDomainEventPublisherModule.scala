package com.bookworm.application.customers.adapter.publisher

import com.google.inject.{AbstractModule, Scopes}

class CustomersDomainEventPublisherModule extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[DomainEventPublisher]).in(Scopes.SINGLETON)
}
