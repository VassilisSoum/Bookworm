package com.bookworm.application.customers.adapter.service.model

import com.bookworm.application.customers.domain.model.{CustomerId, VerificationToken}

case class SaveEmailVerificationTokenServiceModel(verificationToken: VerificationToken, customerId: CustomerId)
