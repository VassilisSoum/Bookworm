package com.bookworm.application.books.service.repository

import cats.effect.IO
import com.bookworm.application.books.dao.BookDao
import com.bookworm.application.books.dao.entity.{AuthorEntity, BookAuthorEntity, BookEntity}
import com.bookworm.application.books.service.repository.model.{Author, AuthorId, Book, BookId}
import doobie.Transactor
import doobie.implicits._

import javax.inject.Inject

trait BookRepository {
  def getBooks: IO[List[Book]]
}

private[repository] class BookRepositoryImpl @Inject()(bookDao: BookDao, transactor: Transactor[IO])
  extends BookRepository {

  override def getBooks: IO[List[Book]] = {
    def retrieveBooks: IO[List[BookAuthorEntity]] =
      bookDao.getAllBooks.transact(transactor)

    def groupAuthorsByBook(books: List[BookAuthorEntity]): Map[BookEntity, List[AuthorEntity]] =
      books.groupMap(_.bookEntity)(_.authorEntity)

    def createBook(bookAuthors: (BookEntity, List[AuthorEntity])): Book =
      Book(
        BookId(bookAuthors._1.bookId),
        bookAuthors._1.title,
        bookAuthors._1.summary,
        bookAuthors._2.map(authorEntity =>
          Author(AuthorId(authorEntity.authorId), authorEntity.firstName, authorEntity.lastName, List.empty)
        ),
        bookAuthors._1.isbn
      )

    retrieveBooks
      .map(groupAuthorsByBook)
      .map(_.map(createBook).toList)
  }
}
