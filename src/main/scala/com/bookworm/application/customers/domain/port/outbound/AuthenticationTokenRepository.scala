package com.bookworm.application.customers.domain.port.outbound

import com.bookworm.application.customers.domain.model.{AuthenticationToken, CustomerId}

trait AuthenticationTokenRepository[F[_]] {
  def saveAuthenticationToken(authenticationToken: AuthenticationToken): F[Unit]
  def removeAuthenticationTokenOfCustomerId(customerId: CustomerId): F[Unit]
  def exists(authenticationToken: AuthenticationToken): F[Boolean]
}
