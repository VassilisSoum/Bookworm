package com.bookworm.application.books.domain.port.outbound

import com.bookworm.application.books.domain.model.{Book, BusinessError, GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery

trait BookRepository[F[_]] {
  def getBooksForGenre(genreId: GenreId, paginationInfo: PaginationInfo): F[BooksByGenreQuery]
  def addBook(book: Book): F[Either[BusinessError, Book]]
}
