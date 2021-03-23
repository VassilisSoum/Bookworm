package com.bookworm.application.customers.domain.model

sealed trait CustomerRegistrationStatus

object CustomerRegistrationStatus {
  final case object Pending extends CustomerRegistrationStatus
  final case object Completed extends CustomerRegistrationStatus
  final case object Expired extends CustomerRegistrationStatus
}