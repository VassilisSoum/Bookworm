package com.bookworm.application.books.domain.port.inbound

import cats.Monad
import cats.implicits._
import com.bookworm.application.books.domain.model.{Book, DomainBusinessError}
import com.bookworm.application.books.domain.port.outbound.{AuthorRepository, BookRepository}

import javax.inject.Inject

class UpdateBookUseCase[F[_]: Monad] @Inject() (
    bookRepository: BookRepository[F],
    authorRepository: AuthorRepository[F]
) {

  def updateBook(book: Book): F[Either[DomainBusinessError, Book]] =
    authorRepository.exist(book.bookDetails.authors).flatMap { allAuthorsExist =>
      if (allAuthorsExist) {
        bookRepository.getById(book.bookId).flatMap {
          case Some(_) => bookRepository.update(book).map(_ => Right(book))
          case None    => Monad[F].pure(Left(DomainBusinessError.BookDoesNotExist))
        }
      } else {
        Monad[F].pure(Left(DomainBusinessError.OneOrMoreAuthorsDoNotExist))
      }
    }
}
