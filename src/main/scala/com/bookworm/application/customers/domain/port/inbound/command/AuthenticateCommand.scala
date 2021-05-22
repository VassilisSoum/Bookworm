package com.bookworm.application.customers.domain.port.inbound.command

import com.bookworm.application.customers.domain.model.{CustomerEmail, CustomerPassword}

case class AuthenticateCommand(email: CustomerEmail, password: CustomerPassword)
