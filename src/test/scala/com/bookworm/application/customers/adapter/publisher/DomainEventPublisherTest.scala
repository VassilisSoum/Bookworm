package com.bookworm.application.customers.adapter.publisher

import cats.effect.IO
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.customers.adapter.service.CustomerRegistrationVerificationEmailProducerService
import com.bookworm.application.customers.adapter.service.model.EmailSendVerificationServiceModel
import com.bookworm.application.customers.domain.model.event.{DomainEvent, DomainEventPublicationStatus, InitialCustomerRegistrationPendingEvent}

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Success

class DomainEventPublisherTest extends AbstractUnitTest {
  val customerRegistrationVerificationEmailProducerService = mock[CustomerRegistrationVerificationEmailProducerService]

  val domainEventPublisher =
    new DomainEventPublisher(customerRegistrationVerificationEmailProducerService)

  "DomainEventPublisher" should {
    "publish an InitialCustomerRegistrationPendingEvent and return Published as response" in {
      val initialCustomerRegistrationPendingEvent = InitialCustomerRegistrationPendingEvent(
        id = UUID.randomUUID(),
        customerId = customerId.id,
        creationDate = LocalDateTime.now(),
        verificationToken = verificationToken,
        customerFirstName.value,
        customerLastName.value,
        customerEmail.value
      )

      val expectedSendEmailVerificationServiceModel = EmailSendVerificationServiceModel(
        customerFirstName = pendingCustomerQueryModel.customerFirstName,
        customerLastName = pendingCustomerQueryModel.customerLastName,
        customerEmail = pendingCustomerQueryModel.customerEmail,
        verificationToken = verificationToken.value
      )

      (customerRegistrationVerificationEmailProducerService.sendRegistrationVerificationEmail _)
        .expects(expectedSendEmailVerificationServiceModel)
        .returns(IO.pure(Success(())))

      domainEventPublisher
        .publish(initialCustomerRegistrationPendingEvent)
        .unsafeRunSync() shouldBe DomainEventPublicationStatus.Published
    }

    "return NotPublished when the event is not supported" in {
      case class NotSupportedEvent(
          override val id: UUID,
          override val customerId: UUID,
          override val creationDate: LocalDateTime
      ) extends DomainEvent

      val notSupportedEvent =
        NotSupportedEvent(id = UUID.randomUUID(), customerId = customerId.id, creationDate = LocalDateTime.now())

      domainEventPublisher.publish(notSupportedEvent).unsafeRunSync() shouldBe DomainEventPublicationStatus.NotPublished
    }
  }
}
