package com.bookworm.application.customers.domain.model.event

import java.time.LocalDateTime
import java.util.UUID

case class CompleteCustomerRegistrationFinishedEvent(
    override val id: UUID,
    override val customerId: UUID,
    override val creationDate: LocalDateTime
) extends DomainEvent
