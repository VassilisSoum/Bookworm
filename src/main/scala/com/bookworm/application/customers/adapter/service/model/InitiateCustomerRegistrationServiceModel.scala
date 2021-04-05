package com.bookworm.application.customers.adapter.service.model

import com.bookworm.application.customers.domain.model.{CustomerAge, CustomerEmail, CustomerFirstName, CustomerId, CustomerLastName, CustomerPassword}

case class InitiateCustomerRegistrationServiceModel(
    id: CustomerId,
    firstName: CustomerFirstName,
    lastName: CustomerLastName,
    email: CustomerEmail,
    age: CustomerAge,
    password: CustomerPassword
)
