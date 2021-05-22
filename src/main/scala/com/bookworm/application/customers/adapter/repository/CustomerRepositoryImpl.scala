package com.bookworm.application.customers.adapter.repository

import com.bookworm.application.customers.adapter.repository.dao.CustomerDao
import com.bookworm.application.customers.domain.model._
import com.bookworm.application.customers.domain.port.inbound.query.CustomerQueryModel
import com.bookworm.application.customers.domain.port.outbound.CustomerRepository
import doobie.ConnectionIO

import javax.inject.Inject

private[repository] class CustomerRepositoryImpl @Inject() (customerDao: CustomerDao)
  extends CustomerRepository[ConnectionIO] {

  override def exists(customerEmail: CustomerEmail): ConnectionIO[Boolean] =
    customerDao.exists(customerEmail)

  override def findBy(customerId: CustomerId): ConnectionIO[Option[CustomerQueryModel]] =
    customerDao.getOptionalByCustomerId(customerId)

  override def findBy(
    customerEmail: CustomerEmail,
    customerPassword: CustomerPassword
  ): ConnectionIO[Option[CustomerQueryModel]] =
    customerDao.getOptionalByCustomerEmailAndPassword(customerEmail, customerPassword)

  override def save(customer: Customer): ConnectionIO[Unit] =
    customerDao.insert(customer)

  override def updateRegistrationStatus(
    customerId: CustomerId,
    customerRegistrationStatus: CustomerRegistrationStatus
  ): ConnectionIO[Unit] =
    customerDao.updateRegistrationStatus(customerId, customerRegistrationStatus)
}
