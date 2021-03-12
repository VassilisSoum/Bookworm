package com.bookworm.application.books.domain.port.outbound

import com.bookworm.application.books.domain.model.AuthorId

trait AuthorRepository[F[_]] {
  def exist(authorIds: List[AuthorId]): F[Boolean]
}
