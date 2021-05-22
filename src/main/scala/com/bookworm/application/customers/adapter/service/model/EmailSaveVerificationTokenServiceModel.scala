package com.bookworm.application.customers.adapter.service.model

import com.bookworm.application.customers.domain.model.{CustomerId, VerificationToken}

case class EmailSaveVerificationTokenServiceModel(verificationToken: VerificationToken, customerId: CustomerId)
