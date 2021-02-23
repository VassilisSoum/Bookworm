package com.bookworm.application.books.adapter.service

import cats.effect.IO
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery
import com.bookworm.application.books.domain.port.inbound.{AddBookUseCase, GetBooksByGenreUseCase, RemoveBookUseCase}
import com.bookworm.application.books.domain.port.outbound.BookRepository

import javax.inject.Inject

class BookServiceImpl @Inject() (bookRepository: BookRepository[IO])
  extends GetBooksByGenreUseCase[IO]
  with AddBookUseCase[IO]
  with RemoveBookUseCase[IO] {

  def retrieveBooksByGenre(
    genre: GenreId,
    paginationInfo: PaginationInfo
  ): IO[BooksByGenreQuery] =
    bookRepository.getAllByGenre(genre, paginationInfo)

  def addBook(book: Book): IO[Either[BusinessError, Book]] =
    bookRepository.add(book)

  def removeBook(bookId: BookId): IO[Either[BusinessError, Unit]] =
    bookRepository.remove(bookId)
}
