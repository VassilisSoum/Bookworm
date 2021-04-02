package com.bookworm.application.customers.domain.port.outbound.event

import java.time.LocalDateTime
import java.util.UUID

case class InitialCustomerRegistrationPendingEvent(
    override val id: UUID,
    override val customerId: UUID,
    override val creationDate: LocalDateTime
) extends OutboundDomainEvent
