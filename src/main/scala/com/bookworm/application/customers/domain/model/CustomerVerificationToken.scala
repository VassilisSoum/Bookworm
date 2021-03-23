package com.bookworm.application.customers.domain.model

import java.time.LocalDateTime

case class CustomerVerificationToken(token: VerificationToken, customerId: CustomerId, expirationDate: LocalDateTime)
