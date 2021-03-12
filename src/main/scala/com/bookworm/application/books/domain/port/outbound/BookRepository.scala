package com.bookworm.application.books.domain.port.outbound

import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.BookQueryModel

trait BookRepository[F[_]] {
  def getAllByGenre(genreId: GenreId, paginationInfo: PaginationInfo): F[List[BookQueryModel]]
  def getById(bookId: BookId): F[Option[BookQueryModel]]
  def add(book: Book): F[Book]
  def remove(bookId: BookId): F[Unit]
  def update(book: Book): F[Book]
}
