package com.bookworm.application.customers.domain.port.inbound.event

sealed trait DomainEventConsumerStatus

object DomainEventConsumerStatus {
  final case object SuccessfullyProcessed extends DomainEventConsumerStatus
  final case object FailedToBeProcessed extends DomainEventConsumerStatus
}