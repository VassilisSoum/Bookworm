package com.bookworm.application.books.dao.entity

import java.util.UUID

final case class BookEntity(bookId: UUID, title: String, summary: String, isbn: String)
