package com.bookworm.application.customers.domain.model

sealed trait DomainValidationError extends DomainError

object DomainValidationError {
  final case object InvalidCustomerFirstName extends DomainValidationError
  final case object InvalidCustomerLastName extends DomainValidationError
  final case object InvalidCustomerEmail extends DomainValidationError
  final case object InvalidCustomerAge extends DomainValidationError
}