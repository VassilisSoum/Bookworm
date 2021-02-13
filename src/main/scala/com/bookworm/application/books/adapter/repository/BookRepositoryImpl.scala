package com.bookworm.application.books.adapter.repository

import cats.effect.Sync
import cats.implicits._
import com.bookworm.application.books.adapter.repository.BookRepositoryImpl.tryCreateContinuationToken
import com.bookworm.application.books.adapter.repository.dao.BookDao
import com.bookworm.application.books.domain.model.{ContinuationToken, GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.query.{BookQueryModel, BooksByGenreQuery}
import com.bookworm.application.books.domain.port.outbound.BookRepository
import doobie.Transactor
import doobie.implicits._

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

//TODO: Create guice module and make this class package private to use only trait
class BookRepositoryImpl[F[_]: Sync] @Inject() (bookDao: BookDao, transactor: Transactor[F]) extends BookRepository[F] {

  override def getBooksForGenre(
    genreId: GenreId,
    paginationInfo: PaginationInfo
  ): F[BooksByGenreQuery] =
    bookDao.getBooks(genreId, paginationInfo).transact(transactor).flatMap { booksByGenre =>
      booksByGenre.lastOption match {
        case Some(bookByGenre) =>
          tryCreateContinuationToken(bookByGenre) match {
            case Success(continuationToken) =>
              Sync[F].delay(BooksByGenreQuery(booksByGenre, Some(continuationToken)))
            case Failure(exception) =>
              Sync[F].raiseError(exception)
          }
        case None =>
          Sync[F].delay(BooksByGenreQuery(List.empty, None))
      }
    }
}

object BookRepositoryImpl {

  def tryCreateContinuationToken(bookQueryModel: BookQueryModel): Try[ContinuationToken] =
    ContinuationToken.create(s"${bookQueryModel.updatedAt}${ContinuationToken.delimiter}${bookQueryModel.id}") match {
      case Right(continuationToken) => Success(continuationToken)
      case Left(_)                  => Failure(new IllegalStateException("Cannot create a continuation token"))
    }
}
