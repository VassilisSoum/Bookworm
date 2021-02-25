package com.bookworm.application.books.adapter.service

import cats.effect.IO
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery
import com.bookworm.application.books.domain.port.inbound.{AddBookUseCase, GetBooksByGenreUseCase, RemoveBookUseCase, UpdateBookUseCase}
import com.bookworm.application.books.domain.port.outbound.BookRepository

import javax.inject.Inject

class BookService @Inject() (bookRepository: BookRepository[IO])
  extends GetBooksByGenreUseCase[IO]
  with AddBookUseCase[IO]
  with RemoveBookUseCase[IO]
  with UpdateBookUseCase[IO] {

  override def retrieveBooksByGenre(
    genre: GenreId,
    paginationInfo: PaginationInfo
  ): IO[BooksByGenreQuery] =
    bookRepository.getAllByGenre(genre, paginationInfo)

  override def addBook(book: Book): IO[Either[DomainBusinessError, Book]] =
    bookRepository.add(book)

  override def removeBook(bookId: BookId): IO[Either[DomainBusinessError, Unit]] =
    bookRepository.remove(bookId)

  override def updateBook(book: Book): IO[Either[DomainBusinessError, Book]] =
    bookRepository.update(book)

}
