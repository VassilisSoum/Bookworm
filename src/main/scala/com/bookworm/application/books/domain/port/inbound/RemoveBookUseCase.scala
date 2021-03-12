package com.bookworm.application.books.domain.port.inbound

import cats.Monad
import cats.implicits._
import com.bookworm.application.books.domain.model.{BookId, DomainBusinessError}
import com.bookworm.application.books.domain.port.outbound.BookRepository

import javax.inject.Inject

class RemoveBookUseCase[F[_]: Monad] @Inject() (bookRepository: BookRepository[F]) {

  def removeBook(bookId: BookId): F[Either[DomainBusinessError, Unit]] =
    bookRepository.getById(bookId).flatMap {
      case Some(_) => bookRepository.remove(bookId).map(_ => Right(()))
      case None    => Monad[F].pure(Left(DomainBusinessError.BookDoesNotExist))
    }
}
