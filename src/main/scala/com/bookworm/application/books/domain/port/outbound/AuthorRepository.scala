package com.bookworm.application.books.domain.port.outbound

import com.bookworm.application.books.domain.model.{AuthorId, BookId}
import com.bookworm.application.books.domain.port.inbound.query.AuthorQueryModel

trait AuthorRepository[F[_]] {
  def exist(authorIds: List[AuthorId]): F[Boolean]
  def getAllByBookId(bookId: BookId): F[List[AuthorQueryModel]]
}
