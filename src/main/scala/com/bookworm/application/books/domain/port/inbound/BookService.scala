package com.bookworm.application.books.domain.port.inbound

import cats.effect.IO
import com.bookworm.application.books.domain.model.{Book, BusinessError, GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery
import com.bookworm.application.books.domain.port.outbound.BookRepository

import javax.inject.Inject

class BookService @Inject() (bookRepository: BookRepository[IO]) {

  def retrieveBooksByGenre(
    genre: GenreId,
    paginationInfo: PaginationInfo
  ): IO[BooksByGenreQuery] =
    bookRepository.getBooksForGenre(genre, paginationInfo)

  def createBook(book: Book): IO[Either[BusinessError, Book]] =
    bookRepository.addBook(book)
}
