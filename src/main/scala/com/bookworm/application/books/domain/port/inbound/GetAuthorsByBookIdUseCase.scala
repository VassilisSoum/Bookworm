package com.bookworm.application.books.domain.port.inbound

import cats.Monad
import cats.implicits._
import com.bookworm.application.books.domain.model.{BookId, DomainBusinessError}
import com.bookworm.application.books.domain.port.inbound.query.AuthorsByBookIdQuery
import com.bookworm.application.books.domain.port.outbound.{AuthorRepository, BookRepository}

import javax.inject.Inject

class GetAuthorsByBookIdUseCase[F[_]: Monad] @Inject() (
    bookRepository: BookRepository[F],
    authorRepository: AuthorRepository[F]
) {

  def retrieveAuthorsByBookId(bookId: BookId): F[Either[DomainBusinessError, AuthorsByBookIdQuery]] =
    bookRepository.getById(bookId).flatMap {
      case Some(_) => authorRepository.getAllByBookId(bookId).map(authors => Right(AuthorsByBookIdQuery(authors)))
      case None    => Monad[F].pure(Left(DomainBusinessError.BookDoesNotExist))
    }
}
