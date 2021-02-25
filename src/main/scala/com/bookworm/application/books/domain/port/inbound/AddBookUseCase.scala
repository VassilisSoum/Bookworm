package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.books.domain.model.{Book, DomainBusinessError}

trait AddBookUseCase[F[_]] {
  def addBook(book: Book): F[Either[DomainBusinessError, Book]]
}
