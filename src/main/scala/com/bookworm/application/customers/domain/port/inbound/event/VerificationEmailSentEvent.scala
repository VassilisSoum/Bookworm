package com.bookworm.application.customers.domain.port.inbound.event

import com.bookworm.application.customers.domain.model.CustomerVerificationToken

import java.time.LocalDateTime

case class VerificationEmailSentEvent(
    verificationToken: CustomerVerificationToken,
    creationDate: LocalDateTime
)
