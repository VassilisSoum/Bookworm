package com.bookworm.application.books.domain.port.outbound

import com.bookworm.application.books.domain.model.{BookId, GenreId}
import com.bookworm.application.books.domain.port.inbound.query.BookWithAuthorQuery

trait BookRepository[F[_]] {
  def getBooksAndAuthorsForGenre(genreId: GenreId): F[Map[BookId, List[BookWithAuthorQuery]]]
}