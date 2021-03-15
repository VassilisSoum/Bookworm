package com.bookworm.application.books.adapter.repository.dao

import cats.implicits._
import com.bookworm.application.books.domain.model.{AuthorId, BookId}
import com.bookworm.application.books.domain.port.inbound.query.AuthorQueryModel
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

  def getAllByBookId(bookId: BookId): doobie.ConnectionIO[List[AuthorQueryModel]] =
    fr"""SELECT author.authorId,author.firstName,author.lastName FROM BOOKWORM.AUTHOR author
          JOIN BOOKWORM.BOOK_AUTHOR book_author 
          ON author.authorId = book_author.authorId 
          WHERE book_author.bookId = ${bookId.id}"""
      .query[AuthorQueryModel]
      .to[List]
}
