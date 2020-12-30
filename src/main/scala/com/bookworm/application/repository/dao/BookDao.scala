package com.bookworm.application.repository.dao

import com.bookworm.application.repository.model.Book
import doobie._
import doobie.implicits._

trait BookDao {
  def getAllBooks: ConnectionIO[List[Book]]
}

private[repository] class BookDaoImpl extends BookDao {

  override def getAllBooks: doobie.ConnectionIO[List[Book]] =
    sql"select * from bookworm.book"
      .query[Book]
      .to[List]
}
