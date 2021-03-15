package com.bookworm.application.books.domain.port.inbound.query

import java.util.UUID

case class AuthorQueryModel(authorId: UUID, firstName: String, lastName: String)

case class AuthorsByBookIdQuery (authors: List[AuthorQueryModel])
