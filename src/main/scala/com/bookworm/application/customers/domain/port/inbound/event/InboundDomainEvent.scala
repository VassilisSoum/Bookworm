package com.bookworm.application.customers.domain.port.inbound.event

import java.time.LocalDateTime
import java.util.UUID

trait InboundDomainEvent {
  def id: UUID
  def customerId: UUID
  def creationDate: LocalDateTime
}
