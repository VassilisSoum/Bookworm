package com.bookworm.application.repository

import cats.effect.IO
import com.bookworm.application.repository.dao.BookDao
import com.bookworm.application.repository.model.Book
import doobie.Transactor
import doobie.implicits._

import javax.inject.Inject

trait BookRepository {
  def getBooks: IO[List[Book]]
}

private[repository] class BookRepositoryImpl @Inject() (bookDao: BookDao, transactor: Transactor[IO])
  extends BookRepository {

  override def getBooks: IO[List[Book]] =
    bookDao.getAllBooks.transact(transactor)
}
