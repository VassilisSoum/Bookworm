package com.bookworm.application.customers.domain.port.outbound.event

sealed trait DomainEventPublicationStatus

object DomainEventPublicationStatus {
  final case object Published extends DomainEventPublicationStatus
  final case object NotPublished extends DomainEventPublicationStatus
}