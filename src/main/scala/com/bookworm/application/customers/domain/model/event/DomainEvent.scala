package com.bookworm.application.customers.domain.model.event

import java.time.LocalDateTime
import java.util.UUID

trait DomainEvent {
  def id: UUID
  def customerId: UUID
  def creationDate: LocalDateTime
}
