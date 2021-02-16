package com.bookworm.application.books.adapter.repository.dao

import cats.implicits._
import com.bookworm.application.books.domain.model.AuthorId
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.fragments._

import javax.inject.Inject

class AuthorDao @Inject() () {

  def allAuthorsExist(authorIds: List[AuthorId]): doobie.ConnectionIO[Boolean] = {
    val fragment = fr"SELECT COUNT(*) FROM BOOKWORM.AUTHOR" ++ whereAndOpt(
      authorIds.toNel.map(authorId => in(fr"authorId", authorId))
    )

    fragment.query[Long].unique.map(_ == authorIds.size)
  }
}
