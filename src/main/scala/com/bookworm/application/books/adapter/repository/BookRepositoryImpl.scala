package com.bookworm.application.books.adapter.repository

import cats.effect.Sync
import com.bookworm.application.books.adapter.repository.dao.BookDao
import com.bookworm.application.books.domain.model.{GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.query.BookQueryModel
import com.bookworm.application.books.domain.port.outbound.BookRepository
import doobie.Transactor
import doobie.implicits._

import javax.inject.Inject

class BookRepositoryImpl[F[_]: Sync] @Inject() (bookDao: BookDao, transactor: Transactor[F]) extends BookRepository[F] {

  override def getBooksForGenre(
    genreId: GenreId,
    paginationInfo: PaginationInfo
  ): F[List[BookQueryModel]] =
    bookDao.getBooks(genreId, paginationInfo).transact(transactor)
}
