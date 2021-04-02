package com.bookworm.application.customers.domain.port.outbound

import com.bookworm.application.customers.domain.model.{Customer, CustomerEmail, CustomerId, CustomerRegistrationStatus}
import com.bookworm.application.customers.domain.port.inbound.query.CustomerQueryModel

trait CustomerRepository[F[_]] {
  def exists(customerEmail: CustomerEmail): F[Boolean]
  def findBy(customerId: CustomerId): F[Option[CustomerQueryModel]]
  def save(customer: Customer): F[Unit]

  def updateRegistrationStatus(
    customerId: CustomerId,
    customerRegistrationStatus: CustomerRegistrationStatus
  ): F[Unit]
}
