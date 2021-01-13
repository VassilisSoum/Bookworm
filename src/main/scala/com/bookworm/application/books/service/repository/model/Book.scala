package com.bookworm.application.books.service.repository.model

import java.util.UUID

case class BookId(id: UUID) extends AnyVal

case class Book(bookId: BookId, title: String, summary: String, authors: List[Author], isbn: String)
