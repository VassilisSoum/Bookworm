package com.bookworm.application.customers.domain.model

sealed trait DomainBusinessError extends DomainError

object DomainBusinessError {
  final case object CustomerAlreadyExists extends DomainBusinessError
  final case object CustomerDoesNotExists extends DomainBusinessError
  final case object CustomerAlreadyRegistered extends DomainBusinessError
  final case object VerificationTokenDoesNotExists extends DomainBusinessError
  final case object VerificationTokenExpired extends DomainBusinessError
}
