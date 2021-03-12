package com.bookworm.application.books.adapter.repository

import com.bookworm.application.books.adapter.repository.dao.BookDao
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.BookQueryModel
import com.bookworm.application.books.domain.port.outbound.BookRepository
import doobie.ConnectionIO

import javax.inject.Inject

private[repository] class BookRepositoryImpl @Inject() (
    bookDao: BookDao
) extends BookRepository[ConnectionIO] {

  override def getAllByGenre(
    genreId: GenreId,
    paginationInfo: PaginationInfo
  ): ConnectionIO[List[BookQueryModel]] =
    bookDao.getBooks(genreId, paginationInfo)

  override def add(book: Book): ConnectionIO[Book] =
    bookDao.insertBook(book).map(_ => book)

  override def getById(bookId: BookId): ConnectionIO[Option[BookQueryModel]] =
    bookDao.getOptionalBookById(bookId)

  override def remove(bookId: BookId): ConnectionIO[Unit] =
    bookDao.softDelete(bookId)

  override def update(book: Book): ConnectionIO[Book] =
    bookDao.updateBook(book).map(_ => book)

}
