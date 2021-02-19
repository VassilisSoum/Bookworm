package com.bookworm.application.books.domain.port.outbound

import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.{BookQueryModel, BooksByGenreQuery}

trait BookRepository[F[_]] {
  def getBooksForGenre(genreId: GenreId, paginationInfo: PaginationInfo): F[BooksByGenreQuery]
  def addBook(book: Book): F[Either[BusinessError, Book]]
  def getBookById(bookId: BookId): F[Either[BusinessError, BookQueryModel]]
}
