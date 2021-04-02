package com.bookworm.application.customers.domain.port.outbound.event

import java.time.LocalDateTime
import java.util.UUID

trait OutboundDomainEvent {
  def id: UUID
  def customerId: UUID
  def creationDate: LocalDateTime
}
