package com.bookworm.application.integration.customers

import cats.effect.IO
import com.bookworm.application.customers.adapter.publisher.DomainEventPublisher
import com.bookworm.application.customers.adapter.service.CustomerRegistrationVerificationEmailProducerService
import com.bookworm.application.customers.domain.model.event.{DomainEvent, DomainEventPublicationStatus}

class FakeDomainEventPublisher(
    customerRegistrationVerificationEmailProducerService: CustomerRegistrationVerificationEmailProducerService
) extends DomainEventPublisher(customerRegistrationVerificationEmailProducerService) {

  override def publish[T <: DomainEvent](event: T): IO[DomainEventPublicationStatus] =
    IO.pure(DomainEventPublicationStatus.Published)
}
