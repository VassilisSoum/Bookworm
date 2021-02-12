package com.bookworm.application.books.adapter.service

import cats.effect._
import cats.implicits._
import com.bookworm.application.books.adapter.service.BookServiceImpl.tryCreateContinuationToken
import com.bookworm.application.books.domain.model.{ContinuationToken, GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.BookService
import com.bookworm.application.books.domain.port.inbound.query.{BookQueryModel, BooksByGenreQuery}
import com.bookworm.application.books.domain.port.outbound.BookRepository

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class BookServiceImpl[F[_]: Sync] @Inject() (bookRepository: BookRepository[F]) extends BookService[F] {

  def retrieveBooksByGenre(
    genre: GenreId,
    paginationInfo: PaginationInfo
  ): F[BooksByGenreQuery] =
    bookRepository.getBooksForGenre(genre, paginationInfo).flatMap { booksByGenre =>
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

object BookServiceImpl {

  def tryCreateContinuationToken(bookQueryModel: BookQueryModel): Try[ContinuationToken] =
    ContinuationToken.create(s"${bookQueryModel.updatedAt}${ContinuationToken.delimiter}${bookQueryModel.id}") match {
      case Right(continuationToken) => Success(continuationToken)
      case Left(_)                  => Failure(new IllegalStateException("Cannot create a continuation token"))
    }
}
