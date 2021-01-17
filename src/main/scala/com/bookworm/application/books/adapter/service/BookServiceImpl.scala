package com.bookworm.application.books.adapter.service

import cats.effect._
import com.bookworm.application.books.domain.model.{BookId, GenreId}
import com.bookworm.application.books.domain.port.inbound.BookService
import com.bookworm.application.books.domain.port.inbound.query.BookWithAuthorQuery
import com.bookworm.application.books.domain.port.outbound.BookRepository

import javax.inject.Inject

class BookServiceImpl[F[_]: Sync] @Inject() (bookRepository: BookRepository[F]) extends BookService[F] {

  def retrieveAllBooksByGenre(genre: GenreId): F[Map[BookId, List[BookWithAuthorQuery]]] =
    bookRepository.getBooksAndAuthorsForGenre(genre)
}
