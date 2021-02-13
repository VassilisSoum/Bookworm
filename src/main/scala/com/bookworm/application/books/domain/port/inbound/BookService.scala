package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.books.domain.model.{Book, BusinessError, GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery

trait BookService[F[_]] {
  def retrieveBooksByGenre(genre: GenreId, paginationInfo: PaginationInfo): F[BooksByGenreQuery]
  def createBook(book: Book): F[Either[BusinessError, Book]]
}
