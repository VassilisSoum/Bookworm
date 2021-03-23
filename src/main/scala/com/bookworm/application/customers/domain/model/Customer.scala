package com.bookworm.application.customers.domain.model

case class Customer(
    customerId: CustomerId,
    customerDetails: CustomerDetails,
    customerRegistrationStatus: CustomerRegistrationStatus
)
