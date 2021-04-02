package com.bookworm.application.customers.adapter.producer

import cats.effect.IO
import com.bookworm.application.customers.domain.port.outbound.event.{DomainEventPublicationStatus, OutboundDomainEvent}
import org.json4s.NoTypeHints
import org.json4s.ext.{JavaTimeSerializers, JavaTypesSerializers}
import org.json4s.jackson.Serialization

import javax.inject.Inject

class DomainEventProducer @Inject() () {

  implicit private val formats =
    Serialization.formats(NoTypeHints) ++ JavaTypesSerializers.all ++ JavaTimeSerializers.all

  def produce[T <: OutboundDomainEvent](event: T): IO[DomainEventPublicationStatus] = {
    //TODO: Implement it with AWS SQS when mail service is created
    IO.pure(DomainEventPublicationStatus.Published)
  }
}
