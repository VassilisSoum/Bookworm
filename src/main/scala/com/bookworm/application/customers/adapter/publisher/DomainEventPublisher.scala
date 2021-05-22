package com.bookworm.application.customers.adapter.publisher

import cats.effect.IO
import com.bookworm.application.customers.adapter.logger
import com.bookworm.application.customers.adapter.service.CustomerRegistrationVerificationEmailProducerService
import com.bookworm.application.customers.adapter.service.model.EmailSendVerificationServiceModel
import com.bookworm.application.customers.domain.model.event.{DomainEvent, DomainEventPublicationStatus, InitialCustomerRegistrationPendingEvent}

import javax.inject.Inject
import scala.util.{Failure, Success}

class DomainEventPublisher @Inject() (
    customerRegistrationVerificationEmailProducerService: CustomerRegistrationVerificationEmailProducerService
) {

  def publish[T <: DomainEvent](event: T): IO[DomainEventPublicationStatus] =
    event match {
      case initialCustomerRegistrationPendingEvent @ InitialCustomerRegistrationPendingEvent(
            _,
            _,
            _,
            _,
            customerFirstName,
            customerLastName,
            customerEmail
          ) =>
        customerRegistrationVerificationEmailProducerService
          .sendRegistrationVerificationEmail(
            EmailSendVerificationServiceModel(
              customerFirstName,
              customerLastName,
              customerEmail,
              initialCustomerRegistrationPendingEvent.verificationToken.value
            )
          )
          .flatMap {
            case Success(_) =>
              IO.delay(logger.debug(s"Publishing event: $initialCustomerRegistrationPendingEvent"))
                .map(_ => DomainEventPublicationStatus.Published)
            case Failure(throwable) =>
              IO.delay(logger.error(s"Cannot publish event $initialCustomerRegistrationPendingEvent. $throwable"))
                .map(_ => DomainEventPublicationStatus.NotPublished)
          }
      case _ => IO.pure(DomainEventPublicationStatus.NotPublished)
    }
}
