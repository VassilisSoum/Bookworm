package com.bookworm.application.integration.customers

import cats.effect.IO
import com.bookworm.application.customers.adapter.publisher.DomainEventPublisher
import com.bookworm.application.customers.adapter.service.{CustomerApplicationService, CustomerRegistrationVerificationEmailProducerService}
import com.bookworm.application.customers.domain.model.event.{DomainEvent, DomainEventPublicationStatus}

class FakeDomainEventPublisher(
    customerRegistrationVerificationEmailProducerService: CustomerRegistrationVerificationEmailProducerService,
    customerApplicationService: CustomerApplicationService
) extends DomainEventPublisher(customerRegistrationVerificationEmailProducerService, customerApplicationService) {

  override def publish[T <: DomainEvent](event: T): IO[DomainEventPublicationStatus] =
    IO.pure(DomainEventPublicationStatus.Published)
}
