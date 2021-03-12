package com.bookworm.application.books.domain.port.inbound

import cats.Monad
import cats.implicits._
import com.bookworm.application.books.domain.model.{Book, DomainBusinessError}
import com.bookworm.application.books.domain.port.outbound.{AuthorRepository, BookRepository}

import javax.inject.Inject

class AddBookUseCase[F[_]: Monad] @Inject() (bookRepository: BookRepository[F], authorRepository: AuthorRepository[F]) {

  def addBook(book: Book): F[Either[DomainBusinessError, Book]] =
    authorRepository.exist(book.bookDetails.authors).flatMap { allAuthorsExist =>
      if (allAuthorsExist) {
        bookRepository.add(book).map(Right(_))
      } else {
        Monad[F].pure(Left(DomainBusinessError.OneOrMoreAuthorsDoNotExist))
      }
    }
}
