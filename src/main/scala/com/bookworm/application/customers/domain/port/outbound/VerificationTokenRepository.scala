package com.bookworm.application.customers.domain.port.outbound

import com.bookworm.application.customers.domain.model.{CustomerId, CustomerVerificationToken, VerificationToken}

trait VerificationTokenRepository[F[_]] {
  def save(customerVerificationToken: CustomerVerificationToken): F[Unit]
  def removeAll(customerId: CustomerId): F[Unit]
  def removeExpiredVerificationTokens(): F[Unit]
  def findBy(verificationToken: VerificationToken): F[Option[CustomerVerificationToken]]
}
