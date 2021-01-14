package com.bookworm.application.books.service.repository.model

import java.util.UUID

case class AuthorId(id: UUID) extends AnyVal

case class Author(authorId: AuthorId, firstName: String, lastName: String)
