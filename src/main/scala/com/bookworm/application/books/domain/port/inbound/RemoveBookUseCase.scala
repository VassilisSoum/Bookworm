package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.books.domain.model.{BookId, DomainBusinessError}

trait RemoveBookUseCase[F[_]] {
  def removeBook(bookId: BookId): F[Either[DomainBusinessError, Unit]]
}
