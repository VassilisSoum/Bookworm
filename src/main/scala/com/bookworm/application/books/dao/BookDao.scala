package com.bookworm.application.books.dao

import com.bookworm.application.books.dao.entity.BookAuthorEntity
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

trait BookDao {
  def getAllBooks: ConnectionIO[List[BookAuthorEntity]]
}

private[dao] class BookDaoImpl extends BookDao {

  override def getAllBooks: doobie.ConnectionIO[List[BookAuthorEntity]] =
    sql"""select b.*, a.* from bookworm.book b
         JOIN bookworm.book_author ba ON ba.bookId = b.bookId
         JOIN bookworm.author a ON ba.authorId = a.authorId
         """
      .query[BookAuthorEntity]
      .to[List]
}
