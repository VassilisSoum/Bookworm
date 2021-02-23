package com.bookworm.application.books.domain.port.outbound

import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.{BookQueryModel, BooksByGenreQuery}

trait BookRepository[F[_]] {
  def getAllByGenre(genreId: GenreId, paginationInfo: PaginationInfo): F[BooksByGenreQuery]
  def getById(bookId: BookId): F[Either[BusinessError, BookQueryModel]]
  def add(book: Book): F[Either[BusinessError, Book]]
  def remove(bookId: BookId): F[Either[BusinessError, Unit]]
}
