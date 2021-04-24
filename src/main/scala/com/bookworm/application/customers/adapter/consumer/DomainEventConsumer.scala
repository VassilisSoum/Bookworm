package com.bookworm.application.customers.adapter.consumer

import cats.effect.IO
import com.bookworm.application.customers.domain.model.event.{DomainEvent, DomainEventConsumerStatus}

import javax.inject.Inject

class DomainEventConsumer @Inject() ( /*verificationTokenApplicationService: VerificationTokenApplicationService*/ ) {

  def consume[T <: DomainEvent]( /*event: T*/ ): IO[DomainEventConsumerStatus] = ???
  /*event match {
      case verificationEmailSentEvent @ VerificationEmailSentEvent(_, _, _) =>
        verificationTokenApplicationService.saveEmailVerificationToken(
          SaveEmailVerificationTokenCommand(
            verificationEmailSentEvent.verificationToken.token,
            verificationEmailSentEvent.customerId,
            verificationEmailSentEvent.
          )
        )
    }*/
}
