package com.bookworm.application.books.adapter.repository.dao

import com.bookworm.application.books.domain.model.GenreId
import com.bookworm.application.books.domain.port.inbound.query.BookWithAuthorQuery
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

trait BookDao {
  def getAllBooks(genreId: GenreId): ConnectionIO[List[BookWithAuthorQuery]]
}

private[dao] class BookDaoImpl extends BookDao {

  override def getAllBooks(genreId: GenreId): doobie.ConnectionIO[List[BookWithAuthorQuery]] =
    sql"""select b.bookId, b.title, b.summary, b.isbn, b.genreId, a.authorId, a.firstName, a.lastName from bookworm.book b
         JOIN bookworm.book_author ba ON ba.bookId = b.bookId
         JOIN bookworm.author a ON ba.authorId = a.authorId
         WHERE b.genreId = ${genreId.id}
         """
      .query[BookWithAuthorQuery]
      .to[List]
}
