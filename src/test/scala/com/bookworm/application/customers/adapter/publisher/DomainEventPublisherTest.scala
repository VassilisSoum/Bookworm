package com.bookworm.application.customers.adapter.publisher

import cats.effect.IO
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.customers.adapter.service.model.SendEmailVerificationServiceModel
import com.bookworm.application.customers.adapter.service.{CustomerApplicationService, CustomerRegistrationVerificationEmailProducerService}
import com.bookworm.application.customers.domain.model.event.{DomainEvent, DomainEventPublicationStatus, InitialCustomerRegistrationPendingEvent}

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Success

class DomainEventPublisherTest extends AbstractUnitTest {
  val customerRegistrationVerificationEmailProducerService = mock[CustomerRegistrationVerificationEmailProducerService]
  val customerApplicationService = mock[CustomerApplicationService]

  val domainEventPublisher =
    new DomainEventPublisher(customerRegistrationVerificationEmailProducerService, customerApplicationService)

  "DomainEventPublisher" should {
    "publish an InitialCustomerRegistrationPendingEvent and return Published as response" in {
      val initialCustomerRegistrationPendingEvent = InitialCustomerRegistrationPendingEvent(
        id = UUID.randomUUID(),
        customerId = customerId.id,
        creationDate = LocalDateTime.now(),
        verificationToken = verificationToken
      )

      val expectedSendEmailVerificationServiceModel = SendEmailVerificationServiceModel(
        customerFirstName = pendingCustomerQueryModel.customerFirstName,
        customerLastName = pendingCustomerQueryModel.customerLastName,
        customerEmail = pendingCustomerQueryModel.customerEmail,
        verificationToken = verificationToken.value
      )

      (customerApplicationService.retrieveCustomerDetails _)
        .expects(customerId)
        .returns(IO.pure(Some(pendingCustomerQueryModel)))

      (customerRegistrationVerificationEmailProducerService.sendRegistrationVerificationEmail _)
        .expects(expectedSendEmailVerificationServiceModel)
        .returns(IO.pure(Success(())))

      domainEventPublisher
        .publish(initialCustomerRegistrationPendingEvent)
        .unsafeRunSync() shouldBe DomainEventPublicationStatus.Published
    }

    "Don't publish the event when the customer id does not exist when " +
    "sending a InitialCustomerRegistrationPendingEvent" in {
      val initialCustomerRegistrationPendingEvent = InitialCustomerRegistrationPendingEvent(
        id = UUID.randomUUID(),
        customerId = customerId.id,
        creationDate = LocalDateTime.now(),
        verificationToken = verificationToken
      )

      (customerApplicationService.retrieveCustomerDetails _)
        .expects(customerId)
        .returns(IO.pure(None))

      (customerRegistrationVerificationEmailProducerService.sendRegistrationVerificationEmail _)
        .expects(*)
        .never()

      domainEventPublisher
        .publish(initialCustomerRegistrationPendingEvent)
        .unsafeRunSync() shouldBe DomainEventPublicationStatus.NotPublished
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
