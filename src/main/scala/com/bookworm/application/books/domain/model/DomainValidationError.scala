package com.bookworm.application.books.domain.model

sealed trait DomainValidationError extends DomainError

object DomainValidationError {
  final case object EmptyBookTitle extends DomainValidationError
  final case object EmptyBookSummary extends DomainValidationError
  final case object EmptyBookIsbn extends DomainValidationError
  final case object InvalidIsbnLength extends DomainValidationError
  final case object EmptyAuthorFirstName extends DomainValidationError
  final case object EmptyAuthorLastName extends DomainValidationError
  final case object EmptyGenreName extends DomainValidationError
  final case object EmptyContinuationToken extends DomainValidationError
  final case object InvalidContinuationTokenFormat extends DomainValidationError
  final case object NonPositivePaginationLimit extends DomainValidationError
  final case object PaginationLimitExceedsMaximum extends DomainValidationError
  final case object EmptyBookAuthorList extends DomainValidationError
  final case object InvalidBookGenre extends DomainValidationError
  final case object InvalidBookId extends DomainValidationError
}
