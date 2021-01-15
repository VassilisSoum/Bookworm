package com.bookworm.application.books.domain.port.inbound.query

import java.util.UUID

case class BookWithAuthorQuery(
    bookId: UUID,
    title: String,
    summary: String,
    isbn: String,
    genreId: UUID,
    authorId: UUID,
    firstName: String,
    lastName: String
)
