package com.bookworm.application.books.adapter.repository

import cats.effect.Sync
import cats.implicits._
import com.bookworm.application.books.adapter.repository.dao.BookDao
import com.bookworm.application.books.domain.model.{BookId, GenreId}
import com.bookworm.application.books.domain.port.inbound.query.BookWithAuthorQuery
import com.bookworm.application.books.domain.port.outbound.BookRepository
import doobie.Transactor
import doobie.implicits._

import javax.inject.Inject

class BookRepositoryImpl[F[_]: Sync] @Inject() (bookDao: BookDao, transactor: Transactor[F])
  extends BookRepository[F] {

  override def getBooksAndAuthorsForGenre(genreId: GenreId): F[Map[BookId, List[BookWithAuthorQuery]]] = {
    def retrieveBooks: F[List[BookWithAuthorQuery]] =
      bookDao.getAllBooks(genreId).transact(transactor)

    def groupAuthorsByBook(books: List[BookWithAuthorQuery]): Map[BookId, List[BookWithAuthorQuery]] =
      books.groupBy(bookWithAuthorQuery => BookId(bookWithAuthorQuery.bookId))

    retrieveBooks
      .map(groupAuthorsByBook)
  }
}
