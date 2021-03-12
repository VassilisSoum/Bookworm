package com.bookworm.application.books.domain.port.inbound

import cats.implicits._
import cats.{Monad, MonadError}
import com.bookworm.application.books.domain.model.{ContinuationToken, GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.query.{BookQueryModel, BooksByGenreQuery}
import com.bookworm.application.books.domain.port.outbound.BookRepository

import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class GetBooksByGenreUseCase[F[_]: Monad] @Inject() (bookRepository: BookRepository[F])(implicit
    M: MonadError[F, Throwable]
) {

  def retrieveBooksByGenre(genre: GenreId, paginationInfo: PaginationInfo): F[BooksByGenreQuery] =
    bookRepository.getAllByGenre(genre, paginationInfo).flatMap { booksByGenre =>
      booksByGenre.lastOption match {
        case Some(bookByGenre) =>
          tryCreateContinuationToken(bookByGenre) match {
            case Success(continuationToken) =>
              Monad[F].pure(BooksByGenreQuery(booksByGenre, Some(continuationToken)))
            case Failure(exception) =>
              M.raiseError(exception)
          }
        case None =>
          Monad[F].pure(BooksByGenreQuery(List.empty, None))
      }
    }

  private def tryCreateContinuationToken(bookQueryModel: BookQueryModel): Try[ContinuationToken] =
    ContinuationToken.create(s"${bookQueryModel.updatedAt}${ContinuationToken.delimiter}${bookQueryModel.id}") match {
      case Right(continuationToken) => Success(continuationToken)
      case Left(_)                  => Failure(new IllegalStateException("Cannot create a continuation token"))
    }
}
