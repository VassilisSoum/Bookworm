package com.bookworm.application.books.adapter.service

import cats.effect._
import com.bookworm.application.books.domain.model.{Book, BusinessError, GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.BookService
import com.bookworm.application.books.domain.port.inbound.query.BooksByGenreQuery
import com.bookworm.application.books.domain.port.outbound.BookRepository

import javax.inject.Inject

class BookServiceImpl[F[_]: Sync] @Inject() (bookRepository: BookRepository[F]) extends BookService[F] {

  def retrieveBooksByGenre(
    genre: GenreId,
    paginationInfo: PaginationInfo
  ): F[BooksByGenreQuery] =
    bookRepository.getBooksForGenre(genre, paginationInfo)

  override def createBook(book: Book): F[Either[BusinessError, Book]] = ???
}
