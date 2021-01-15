package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.books.domain.model.{BookId, GenreId}
import com.bookworm.application.books.domain.port.inbound.query.BookWithAuthorQuery

trait BookService[F[_]] {
  def retrieveAllBooksByGenre(genre: GenreId): F[Map[BookId, List[BookWithAuthorQuery]]]
}
