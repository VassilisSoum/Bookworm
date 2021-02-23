package com.bookworm.application.books.domain.port.inbound

import com.bookworm.application.books.domain.model.{BookId, BusinessError}

trait RemoveBookUseCase[F[_]] {
  def removeBook(bookId: BookId): F[Either[BusinessError, Unit]]
}
