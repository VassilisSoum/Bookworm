package com.bookworm.application.customers.domain.model

sealed trait CustomerRegistrationStatus

object CustomerRegistrationStatus {
  final case object Pending extends CustomerRegistrationStatus
  final case object Completed extends CustomerRegistrationStatus
  final case object Expired extends CustomerRegistrationStatus

  def fromRegistrationStatus(registrationStatus: String): CustomerRegistrationStatus = registrationStatus match {
    case "Pending"   => CustomerRegistrationStatus.Pending
    case "Completed" => CustomerRegistrationStatus.Completed
    case "Expired"   => CustomerRegistrationStatus.Expired
  }
}
