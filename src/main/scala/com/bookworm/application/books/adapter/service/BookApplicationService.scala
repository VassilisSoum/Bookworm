package com.bookworm.application.books.adapter.service

import cats.effect.IO
import cats.implicits._
import com.bookworm.application.books.adapter.logger
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery
import com.bookworm.application.books.domain.port.inbound.{AddBookUseCase, GetBooksByGenreUseCase, RemoveBookUseCase, UpdateBookUseCase}
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import javax.inject.Inject

class BookApplicationService @Inject() (
    getBooksByGenreUseCase: GetBooksByGenreUseCase[ConnectionIO],
    addBookUseCase: AddBookUseCase[ConnectionIO],
    removeBookUseCase: RemoveBookUseCase[ConnectionIO],
    updateBookUseCase: UpdateBookUseCase[ConnectionIO],
    transactor: Transactor[IO]
) {

  def retrieveBooksByGenre(
    genre: GenreId,
    paginationInfo: PaginationInfo
  ): IO[BooksByGenreQuery] =
    getBooksByGenreUseCase
      .retrieveBooksByGenre(genre, paginationInfo)
      .transact(transactor)
      .attempt
      .flatTap {
        case Left(failure) =>
          IO.pure(logger.error(s"Failure to retrieve books by genre ${genre.id}. $failure"))
        case Right(_) =>
          IO.pure(logger.info(s"Successfully retrieved books for genre ${genre.id}"))
      }
      .flatMap {
        case Left(failure) =>
          IO.raiseError(failure)
        case Right(booksByGenreQuery) =>
          IO.pure(booksByGenreQuery)
      }

  def addBook(book: Book): IO[Either[DomainBusinessError, Book]] =
    addBookUseCase
      .addBook(book)
      .transact(transactor)
      .flatTap(bookE => IO.pure(logger.info(s"Result of adding book $book was $bookE")))

  def removeBook(bookId: BookId): IO[Either[DomainBusinessError, Unit]] =
    removeBookUseCase
      .removeBook(bookId)
      .transact(transactor)
      .flatTap(bookE => IO.pure(logger.info(s"Result of removing a book with $bookId was $bookE")))

  def updateBook(book: Book): IO[Either[DomainBusinessError, Book]] =
    updateBookUseCase
      .updateBook(book)
      .transact(transactor)
      .flatTap(bookE => IO.pure(logger.info(s"Result of updating book $book was $bookE")))
}
