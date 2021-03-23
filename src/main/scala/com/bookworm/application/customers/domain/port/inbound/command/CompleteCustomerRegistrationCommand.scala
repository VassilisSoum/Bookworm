package com.bookworm.application.customers.domain.port.inbound.command

import com.bookworm.application.customers.domain.model.VerificationToken

case class CompleteCustomerRegistrationCommand(verificationToken: VerificationToken)
