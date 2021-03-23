package com.bookworm.application.customers.domain.port.outbound.event

import com.bookworm.application.customers.domain.model.CustomerId

case class CompleteCustomerRegistrationFinishedEvent(customerId: CustomerId)
