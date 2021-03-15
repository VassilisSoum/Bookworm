package com.bookworm.application.books.adapter.service

import cats.effect.IO
import cats.implicits._
import com.bookworm.application.books.adapter.logger
import com.bookworm.application.books.domain.model.{BookId, DomainBusinessError}
import com.bookworm.application.books.domain.port.inbound.GetAuthorsByBookIdUseCase
import com.bookworm.application.books.domain.port.inbound.query.AuthorsByBookIdQuery
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import javax.inject.Inject

class AuthorApplicationService @Inject() (
    getAuthorsByBookIdUseCase: GetAuthorsByBookIdUseCase[ConnectionIO],
    transactor: Transactor[IO]
) {

  def retrieveAuthorsByBookId(bookId: BookId): IO[Either[DomainBusinessError, AuthorsByBookIdQuery]] =
    getAuthorsByBookIdUseCase
      .retrieveAuthorsByBookId(bookId)
      .transact(transactor)
      .flatTap(resultE => IO.pure(logger.info(s"Retrieving authors by book Id ${bookId.id} is $resultE")))
}
