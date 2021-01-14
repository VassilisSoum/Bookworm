package com.bookworm.application.books.dao.query

import java.util.UUID

case class BookWithAuthor(
    bookId: UUID,
    title: String,
    summary: String,
    isbn: String,
    genreId: UUID,
    authorId: UUID,
    firstName: String,
    lastName: String
)
