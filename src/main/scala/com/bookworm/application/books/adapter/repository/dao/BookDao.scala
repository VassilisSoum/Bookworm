package com.bookworm.application.books.adapter.repository.dao

import com.bookworm.application.books.domain.model.{GenreId, PaginationInfo}
import com.bookworm.application.books.domain.port.inbound.query.BookQueryModel
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres.implicits._
import doobie.implicits.javatime._
import doobie.util.fragment

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject

class BookDao @Inject() () {

  def getBooks(genreId: GenreId, paginationInfo: PaginationInfo): doobie.ConnectionIO[List[BookQueryModel]] = {
    def createPaginationStatement(continuationToken: String): fragment.Fragment = {
      val elements = continuationToken.split("_")
      val timestamp = Timestamp.valueOf(LocalDateTime.parse(elements.head))
      val id = elements(1).toLong
      val now = Timestamp.valueOf(LocalDateTime.now())

      fr"""(updatedAt < $timestamp OR (updatedAt = $timestamp AND id < $id)) AND updatedAt > $now
          ORDER BY updatedAt desc, id desc;"""
    }

    def orderByStatement: fragment.Fragment = fr"ORDER BY updatedAt desc, id desc;"

    def createSelectBooksByGenreStatement(genreId: GenreId): fragment.Fragment =
      fr"""SELECT b.bookId, b.title, b.summary, b.isbn,
           g.genreName, b.updatedAt, b.id
         FROM bookworm.book b
         JOIN bookworm.genre g ON g.genreId = b.genreId
         WHERE b.genreId = ${genreId.id}
        """

    (paginationInfo.continuationToken match {
      case Some(continuationToken) =>
        createSelectBooksByGenreStatement(genreId) ++ createPaginationStatement(
          continuationToken.continuationToken
        ) ++ orderByStatement
      case None =>
        createSelectBooksByGenreStatement(genreId) ++ orderByStatement
    })
      .query[BookQueryModel]
      .to[List]
  }
}
