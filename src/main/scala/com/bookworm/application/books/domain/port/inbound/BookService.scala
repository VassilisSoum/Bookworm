package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery

trait BookService[F[_]] {

  def retrieveBooksByGenre(genre: GenreId, paginationInfo: PaginationInfo): F[BooksByGenreQuery]

  def addBook(book: Book): F[Either[BusinessError, Book]]

  def removeBook(bookId: BookId): F[Either[BusinessError, Unit]]
}
