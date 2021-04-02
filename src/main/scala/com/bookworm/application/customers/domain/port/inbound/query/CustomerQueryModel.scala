package com.bookworm.application.customers.domain.port.inbound.query

import com.bookworm.application.customers.domain.model._

import java.util.UUID

case class CustomerQueryModel(
    customerId: UUID,
    customerFirstName: String,
    customerLastName: String,
    customerEmail: String,
    customerAge: Int,
    customerRegistrationStatus: CustomerRegistrationStatus
)
