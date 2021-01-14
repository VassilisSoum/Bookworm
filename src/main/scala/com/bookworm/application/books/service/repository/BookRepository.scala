package com.bookworm.application.books.service.repository

import cats.effect.IO
import com.bookworm.application.books.dao.BookDao
import com.bookworm.application.books.dao.query.BookWithAuthor
import com.bookworm.application.books.service.repository.model.{Author, AuthorId, Book, BookId}
import doobie.Transactor
import doobie.implicits._

import java.util.UUID
import javax.inject.Inject

trait BookRepository {
  def getBooksAndAuthorsForGenre(genreId: UUID): IO[Map[Book, List[Author]]]
}

private[repository] class BookRepositoryImpl @Inject() (bookDao: BookDao, transactor: Transactor[IO])
  extends BookRepository {

  override def getBooksAndAuthorsForGenre(genreId: UUID): IO[Map[Book, List[Author]]] = {
    def retrieveBooks: IO[List[BookWithAuthor]] =
      bookDao.getAllBooks(genreId).transact(transactor)

    def groupAuthorsByBook(books: List[BookWithAuthor]): Map[Book, List[Author]] =
      books.groupMap(key => Book(BookId(key.bookId), key.title, key.summary, key.isbn))(bookWithAuthor =>
        Author(AuthorId(bookWithAuthor.authorId), bookWithAuthor.firstName, bookWithAuthor.lastName)
      )

    retrieveBooks
      .map(groupAuthorsByBook)
  }
}
