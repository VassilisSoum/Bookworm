package com.bookworm.application.customers.adapter.repository

import com.bookworm.application.customers.adapter.repository.dao.CustomerVerificationTokenDao
import com.bookworm.application.customers.domain.model.{CustomerId, CustomerVerificationToken, VerificationToken}
import com.bookworm.application.customers.domain.port.outbound.VerificationTokenRepository
import doobie.ConnectionIO

import javax.inject.Inject

private[repository] class VerificationTokenRepositoryImpl @Inject() (
    customerVerificationTokenDao: CustomerVerificationTokenDao
) extends VerificationTokenRepository[ConnectionIO] {

  override def save(customerVerificationToken: CustomerVerificationToken): ConnectionIO[Unit] =
    customerVerificationTokenDao.insert(customerVerificationToken)

  override def removeAll(customerId: CustomerId): ConnectionIO[Unit] =
    customerVerificationTokenDao.deleteAll(customerId)

  override def removeExpiredVerificationTokens(): ConnectionIO[Unit] =
    customerVerificationTokenDao.deleteAllExpiredVerificationTokens()

  override def findBy(verificationToken: VerificationToken): ConnectionIO[Option[CustomerVerificationToken]] =
    customerVerificationTokenDao.getOptionalCustomerVerificationTokenBy(verificationToken)
}
