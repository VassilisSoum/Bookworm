package com.bookworm.application.books.domain.model

sealed trait ValidationError extends Product with Serializable

object ValidationError {
  final case object EmptyBookTitle extends ValidationError
  final case object EmptyBookSummary extends ValidationError
  final case object EmptyBookIsbn extends ValidationError
  final case object InvalidIsbnLength extends ValidationError
  final case object EmptyAuthorFirstName extends ValidationError
  final case object EmptyAuthorLastName extends ValidationError
  final case object EmptyGenreName extends ValidationError
  final case object EmptyContinuationToken extends ValidationError
  final case object InvalidContinuationTokenFormat extends ValidationError
  final case object NonPositivePaginationLimit extends ValidationError
  final case object PaginationLimitExceedsMaximum extends ValidationError
  final case object EmptyBookAuthorList extends ValidationError
  final case object InvalidBookGenre extends ValidationError
}