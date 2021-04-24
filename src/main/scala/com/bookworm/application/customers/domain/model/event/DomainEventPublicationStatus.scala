package com.bookworm.application.customers.domain.model.event

sealed trait DomainEventPublicationStatus

object DomainEventPublicationStatus {
  final case object Published extends DomainEventPublicationStatus
  final case object NotPublished extends DomainEventPublicationStatus
}