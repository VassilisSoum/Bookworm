package com.bookworm.application.books.domain.port.inbound.query

import com.bookworm.application.books.domain.model.ContinuationToken

import java.time.LocalDateTime
import java.util.UUID

case class BookQueryModel(
    bookId: UUID,
    title: String,
    summary: String,
    isbn: String,
    genre: String,
    updatedAt: LocalDateTime,
    id: Long
)

case class BooksByGenreQuery(
    books: List[BookQueryModel],
    continuationToken: Option[ContinuationToken]
)
