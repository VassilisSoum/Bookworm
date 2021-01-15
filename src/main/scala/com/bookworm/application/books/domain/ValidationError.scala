package com.bookworm.application.books.domain

sealed trait ValidationError

object ValidationError {
  final case object EmptyBookTitle extends ValidationError
  final case object EmptyBookSummary extends ValidationError
  final case object EmptyBookIsbn extends ValidationError
  final case object EmptyAuthorFirstName extends ValidationError
  final case object EmptyAuthorLastName extends ValidationError
}