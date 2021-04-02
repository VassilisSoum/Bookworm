package com.bookworm.application.customers.domain.port.inbound.event

import com.bookworm.application.customers.domain.model.CustomerVerificationToken

import java.time.LocalDateTime
import java.util.UUID

case class VerificationEmailSentEvent(
    override val id: UUID,
    override val customerId: UUID,
    override val creationDate: LocalDateTime,
    verificationToken: CustomerVerificationToken
) extends InboundDomainEvent
