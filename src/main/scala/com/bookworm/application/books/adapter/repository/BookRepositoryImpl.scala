package com.bookworm.application.books.adapter.repository

import cats.effect.IO
import com.bookworm.application.books.adapter.repository.BookRepositoryImpl.tryCreateContinuationToken
import com.bookworm.application.books.adapter.repository.dao.{AuthorDao, BookDao}
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.{BookQueryModel, BooksByGenreQuery}
import com.bookworm.application.books.domain.port.outbound.BookRepository
import doobie.Transactor
import doobie.implicits._

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

private[repository] class BookRepositoryImpl @Inject() (
    bookDao: BookDao,
    authorDao: AuthorDao,
    transactor: Transactor[IO]
) extends BookRepository[IO] {

  override def getBooksForGenre(
    genreId: GenreId,
    paginationInfo: PaginationInfo
  ): IO[BooksByGenreQuery] =
    bookDao.getBooks(genreId, paginationInfo).transact(transactor).flatMap { booksByGenre =>
      booksByGenre.lastOption match {
        case Some(bookByGenre) =>
          tryCreateContinuationToken(bookByGenre) match {
            case Success(continuationToken) =>
              IO.pure(BooksByGenreQuery(booksByGenre, Some(continuationToken)))
            case Failure(exception) =>
              IO.raiseError(exception)
          }
        case None =>
          IO.pure(BooksByGenreQuery(List.empty, None))
      }
    }

  override def addBook(book: Book): IO[Either[BusinessError, Book]] =
    authorDao.allAuthorsExist(book.bookDetails.authors).transact(transactor).flatMap { allAuthorsExist =>
      if (allAuthorsExist) {
        bookDao.insertBook(book).transact(transactor).map(_ => Right(book))
      } else {
        IO.pure(Left(BusinessError.OneOrMoreAuthorsDoNotExist))
      }
    }

  override def getBookById(bookId: BookId): IO[Either[BusinessError, BookQueryModel]] =
    bookDao.getOptionalBookById(bookId).transact(transactor).map {
      case Some(bookQueryModel) => Right(bookQueryModel)
      case None                 => Left(BusinessError.BookDoesNotExist)
    }
}

object BookRepositoryImpl {

  def tryCreateContinuationToken(bookQueryModel: BookQueryModel): Try[ContinuationToken] =
    ContinuationToken.create(s"${bookQueryModel.updatedAt}${ContinuationToken.delimiter}${bookQueryModel.id}") match {
      case Right(continuationToken) => Success(continuationToken)
      case Left(_)                  => Failure(new IllegalStateException("Cannot create a continuation token"))
    }
}
