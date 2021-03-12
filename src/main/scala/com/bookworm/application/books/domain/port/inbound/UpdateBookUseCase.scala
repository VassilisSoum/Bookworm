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
        bookRepository.update(book).map(_ => Right(book))
      } else {
        Monad[F].pure(Left(DomainBusinessError.OneOrMoreAuthorsDoNotExist))
      }
    }
}
