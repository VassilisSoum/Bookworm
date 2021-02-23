package com.bookworm.application.books.adapter.repository.dao

import com.bookworm.application.books.domain.model.BookStatus.{Available, Unavailable}
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.BookQueryModel
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.implicits.javatime._
import doobie.postgres.implicits._
import doobie.util.fragment
import doobie.util.update.Update

import java.sql.Timestamp
import java.time.{Clock, LocalDateTime}
import java.util.UUID
import javax.inject.Inject

class BookDao @Inject() (clock: Clock) {

  def getBooks(genreId: GenreId, paginationInfo: PaginationInfo): doobie.ConnectionIO[List[BookQueryModel]] = {
    def createContinuationTokenStatement(continuationToken: String): fragment.Fragment = {
      val elements = continuationToken.split("_")
      val timestamp = Timestamp.valueOf(LocalDateTime.parse(elements.head))
      val id = elements(1).toLong
      val now = Timestamp.valueOf(LocalDateTime.now())

      fr"""AND (updatedAt < $timestamp OR (updatedAt = $timestamp AND id < $id)) AND updatedAt > $now"""
    }

    def orderByStatement: fragment.Fragment = fr"ORDER BY updatedAt desc, id desc"

    def limitStatement(limit: Int): fragment.Fragment = fr"LIMIT $limit"

    def createSelectBooksByGenreStatement(genreId: GenreId): fragment.Fragment =
      fr"""SELECT b.bookId, b.title, b.summary, b.isbn,
           g.genreName, b.updatedAt, b.id
         FROM bookworm.book b
         JOIN bookworm.genre g ON g.genreId = b.genreId
         WHERE b.genreId = ${genreId.id} AND b.deleted = false
        """

    (paginationInfo.continuationToken match {
      case Some(continuationToken) =>
        createSelectBooksByGenreStatement(genreId) ++ createContinuationTokenStatement(
          continuationToken.continuationToken
        ) ++ orderByStatement ++ limitStatement(paginationInfo.limit.paginationLimit)
      case None =>
        createSelectBooksByGenreStatement(genreId) ++ orderByStatement ++ limitStatement(
          paginationInfo.limit.paginationLimit
        )
    })
      .query[BookQueryModel]
      .to[List]
  }

  def insertBook(book: Book): doobie.ConnectionIO[Unit] = {
    val currentTimestamp = Timestamp.valueOf(LocalDateTime.now(clock))
    val bookAuthorData = book.bookDetails.authors.flatMap(authorId => List((book.bookId.id, authorId.id)))
    val deleted = book.bookStatus match {
      case Available   => false
      case Unavailable => true
    }
    for {
      _ <- sql"""INSERT INTO BOOKWORM.BOOK(bookId,title,summary,isbn,genreId,deleted,createdAt,updatedAt)
         VALUES (
         ${book.bookId.id},
         ${book.bookDetails.title.value},
         ${book.bookDetails.summary.value},
         ${book.bookDetails.isbn.value},${book.bookDetails.genre.id},
         $deleted,
         $currentTimestamp,
         $currentTimestamp
        )""".update.run
      _ <- Update[(UUID, UUID)]("INSERT INTO BOOKWORM.BOOK_AUTHOR(bookId, authorId) VALUES (?, ?)")
        .updateMany(bookAuthorData)
    } yield ()
  }

  def getOptionalBookById(bookId: BookId): doobie.ConnectionIO[Option[BookQueryModel]] =
    fr"""SELECT b.bookId, b.title, b.summary, b.isbn,
           g.genreName, b.updatedAt, b.id
         FROM bookworm.book b
         JOIN bookworm.genre g ON g.genreId = b.genreId
         WHERE b.bookId = ${bookId.id} AND b.deleted = false
        """.query[BookQueryModel].option

  def softDelete(bookId: BookId): doobie.ConnectionIO[BookStatus] = {
    val currentTimestamp = Timestamp.valueOf(LocalDateTime.now(clock))
    fr"""update bookworm.book set deleted = true, updatedAt = $currentTimestamp 
        WHERE bookId = ${bookId.id} AND deleted = false""".update.run
      .map { rowsAffected =>
        if (rowsAffected == 0) BookStatus.Available
        else BookStatus.Unavailable
      }
  }
}
