package com.bookworm.application.books.domain.model

sealed trait BusinessError extends Product with Serializable

object BusinessError {
  final case object BookAlreadyExists extends BusinessError
  final case object OneOrMoreAuthorsDoNotExist extends BusinessError

}
