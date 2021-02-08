package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.books.domain.model.{GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery

trait BookService[F[_]] {
  def retrieveBooksByGenre(genre: GenreId, paginationInfo: PaginationInfo): F[BooksByGenreQuery]
}
