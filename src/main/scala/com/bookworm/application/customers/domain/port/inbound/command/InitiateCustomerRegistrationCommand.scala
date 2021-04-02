package com.bookworm.application.customers.domain.port.inbound.command

import com.bookworm.application.customers.domain.model._

case class InitiateCustomerRegistrationCommand(
    id: CustomerId,
    firstName: CustomerFirstName,
    lastName: CustomerLastName,
    email: CustomerEmail,
    age: CustomerAge,
    password: CustomerPassword
) {

  def toDomainObject: Customer =
    Customer(
      customerId = id,
      customerDetails = CustomerDetails(firstName, lastName, email, age),
      customerPassword = password,
      customerRegistrationStatus = CustomerRegistrationStatus.Pending
    )
}
