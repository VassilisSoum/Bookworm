package com.bookworm.application.books.domain.port.outbound

import com.bookworm.application.books.domain.model.{GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery

trait BookRepository[F[_]] {
  def getBooksForGenre(genreId: GenreId, paginationInfo: PaginationInfo): F[BooksByGenreQuery]
}
