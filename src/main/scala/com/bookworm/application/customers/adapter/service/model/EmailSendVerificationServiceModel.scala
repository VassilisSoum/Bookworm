package com.bookworm.application.customers.adapter.service.model

import java.util.UUID

case class EmailSendVerificationServiceModel(
    customerFirstName: String,
    customerLastName: String,
    customerEmail: String,
    verificationToken: UUID
)
