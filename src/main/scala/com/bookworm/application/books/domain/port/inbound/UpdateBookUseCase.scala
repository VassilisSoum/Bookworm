package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.books.domain.model.{Book, DomainBusinessError}

trait UpdateBookUseCase[F[_]] {
  def updateBook(book: Book): F[Either[DomainBusinessError, Book]]
}
