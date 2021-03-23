package com.bookworm.application.customers.domain.port.inbound.command

import com.bookworm.application.customers.domain.model._

case class InitiateCustomerRegistrationCommand(
    id: CustomerId,
    firstName: CustomerFirstName,
    lastName: CustomerLastName,
    email: CustomerEmail,
    age: CustomerAge
) {

  def toDomainObject: Customer =
    Customer(id, CustomerDetails(firstName, lastName, email, age), CustomerRegistrationStatus.Pending)
}
