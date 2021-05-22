package com.bookworm.application.customers.domain.port.outbound

import com.bookworm.application.customers.domain.model.{Customer, CustomerEmail, CustomerId, CustomerPassword, CustomerRegistrationStatus}
import com.bookworm.application.customers.domain.port.inbound.query.CustomerQueryModel

trait CustomerRepository[F[_]] {
  def exists(customerEmail: CustomerEmail): F[Boolean]
  def findBy(customerEmail: CustomerEmail, customerPassword: CustomerPassword): F[Option[CustomerQueryModel]]
  def findBy(customerId: CustomerId): F[Option[CustomerQueryModel]]
  def save(customer: Customer): F[Unit]

  def updateRegistrationStatus(
    customerId: CustomerId,
    customerRegistrationStatus: CustomerRegistrationStatus
  ): F[Unit]
}
