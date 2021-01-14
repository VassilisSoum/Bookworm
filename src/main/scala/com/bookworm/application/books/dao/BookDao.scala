package com.bookworm.application.books.dao

import com.bookworm.application.books.dao.query.BookWithAuthor
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import java.util.UUID

trait BookDao {
  def getAllBooks(genreId: UUID): ConnectionIO[List[BookWithAuthor]]
}

private[dao] class BookDaoImpl extends BookDao {

  override def getAllBooks(genreId: UUID): doobie.ConnectionIO[List[BookWithAuthor]] =
    sql"""select b.bookId, b.title, b.summary, b.isbn, b.genreId, a.authorId, a.firstName, a.lastName from bookworm.book b
         JOIN bookworm.book_author ba ON ba.bookId = b.bookId
         JOIN bookworm.author a ON ba.authorId = a.authorId
         WHERE b.genreId = $genreId
         """
      .query[BookWithAuthor]
      .to[List]
}
