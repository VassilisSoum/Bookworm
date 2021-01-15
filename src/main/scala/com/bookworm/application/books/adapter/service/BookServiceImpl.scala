package com.bookworm.application.books.adapter.service

import cats.effect.IO
import cats.implicits._
import com.bookworm.application.books.domain.model.{BookId, GenreId}
import com.bookworm.application.books.domain.port.inbound.BookService
import com.bookworm.application.books.domain.port.inbound.query.BookWithAuthorQuery
import com.bookworm.application.books.domain.port.outbound.BookRepository

import javax.inject.Inject

class BookServiceImpl @Inject() (bookRepository: BookRepository[IO]) extends BookService[IO] {

  def retrieveAllBooksByGenre(genre: GenreId): IO[Map[BookId, List[BookWithAuthorQuery]]] =
    bookRepository.getBooksAndAuthorsForGenre(genre)
}
