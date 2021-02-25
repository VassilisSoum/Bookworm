package com.bookworm.application.books.domain.port.outbound

import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.{BookQueryModel, BooksByGenreQuery}

trait BookRepository[F[_]] {
  def getAllByGenre(genreId: GenreId, paginationInfo: PaginationInfo): F[BooksByGenreQuery]
  def getById(bookId: BookId): F[Either[DomainError, BookQueryModel]]
  def add(book: Book): F[Either[DomainBusinessError, Book]]
  def remove(bookId: BookId): F[Either[DomainBusinessError, Unit]]
  def update(book: Book): F[Either[DomainBusinessError, Book]]
}
