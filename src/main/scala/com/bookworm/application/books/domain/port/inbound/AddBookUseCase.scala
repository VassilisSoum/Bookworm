package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.books.domain.model.{Book, BusinessError}

trait AddBookUseCase[F[_]] {
  def addBook(book: Book): F[Either[BusinessError, Book]]
}
