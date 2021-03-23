package com.bookworm.application.customers.domain.port.outbound

import com.bookworm.application.customers.domain.model.{Customer, CustomerEmail, CustomerId}

trait CustomerRepository[F[_]] {
  def exists(customerEmail: CustomerEmail): F[Boolean]
  def findBy(customerId: CustomerId): F[Option[Customer]]
  def save(customer: Customer): F[Unit]
  def update(customer: Customer): F[Unit]
}
