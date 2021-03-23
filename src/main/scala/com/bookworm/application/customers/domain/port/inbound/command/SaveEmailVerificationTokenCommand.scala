package com.bookworm.application.customers.domain.port.inbound.command

import com.bookworm.application.customers.domain.model.{CustomerId, CustomerVerificationToken, VerificationToken}

import java.time.LocalDateTime

case class SaveEmailVerificationTokenCommand(
    token: VerificationToken,
    customerId: CustomerId,
    expirationDate: LocalDateTime
) {

  def toDomainObject: CustomerVerificationToken =
    CustomerVerificationToken(token, customerId, expirationDate)
}
