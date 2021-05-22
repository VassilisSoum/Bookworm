package com.bookworm.application.customers.domain.model

case class AuthenticationToken(customerId: CustomerId, token: String, expirationInSeconds: Int)
