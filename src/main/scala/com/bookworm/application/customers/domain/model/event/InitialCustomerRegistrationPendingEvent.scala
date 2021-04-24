package com.bookworm.application.customers.domain.model.event

import com.bookworm.application.customers.domain.model.VerificationToken

import java.time.LocalDateTime
import java.util.UUID

case class InitialCustomerRegistrationPendingEvent(
    override val id: UUID,
    override val customerId: UUID,
    override val creationDate: LocalDateTime,
    verificationToken: VerificationToken
) extends DomainEvent
