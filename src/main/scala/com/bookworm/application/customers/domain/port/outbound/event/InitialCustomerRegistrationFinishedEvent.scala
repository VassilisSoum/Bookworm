package com.bookworm.application.customers.domain.port.outbound.event

import com.bookworm.application.customers.domain.model.CustomerId

import java.time.LocalDateTime

case class InitialCustomerRegistrationFinishedEvent(
    customerId: CustomerId,
    creationDate: LocalDateTime
)
