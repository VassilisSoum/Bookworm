package com.bookworm.application.books.domain.model

sealed trait DomainBusinessError extends DomainError

object DomainBusinessError {
  final case object OneOrMoreAuthorsDoNotExist extends DomainBusinessError
  final case object BookDoesNotExist extends DomainBusinessError
}
