package com.bookworm.application.books.adapter.repository

import cats.effect.IO
import com.bookworm.application.books.adapter.repository.dao.BookDao
import com.bookworm.application.books.domain.model.{BookId, GenreId}
import com.bookworm.application.books.domain.port.inbound.query.BookWithAuthorQuery
import com.bookworm.application.books.domain.port.outbound.BookRepository
import doobie.Transactor
import doobie.implicits._

import javax.inject.Inject

class BookRepositoryImpl @Inject() (bookDao: BookDao, transactor: Transactor[IO]) extends BookRepository[IO] {

  override def getBooksAndAuthorsForGenre(genreId: GenreId): IO[Map[BookId, List[BookWithAuthorQuery]]] = {
    def retrieveBooks: IO[List[BookWithAuthorQuery]] =
      bookDao.getAllBooks(genreId).transact(transactor)

    def groupAuthorsByBook(books: List[BookWithAuthorQuery]): Map[BookId, List[BookWithAuthorQuery]] =
      books.groupBy(bookWithAuthorQuery => BookId(bookWithAuthorQuery.bookId))

    retrieveBooks
      .map(groupAuthorsByBook)
  }
}
