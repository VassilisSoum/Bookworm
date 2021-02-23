package com.bookworm.application.books.domain.model

sealed trait BookStatus

object BookStatus {
  final case object Available extends BookStatus
  final case object Unavailable extends BookStatus
}
