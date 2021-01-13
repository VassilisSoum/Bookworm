package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.IntegrationTestModule
import com.bookworm.application.books.dao.entity.{AuthorEntity, BookAuthorEntity, BookEntity}
import com.bookworm.application.books.service.repository.model.{AuthorId, BookId}
import com.bookworm.application.books.rest.BookRestApi
import com.bookworm.application.books.rest.dto.{AuthorResponseDto, BookResponseDto}
import doobie._
import doobie.implicits._
import doobie.util.update.Update
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
import org.http4s.{Method, Request, Status}
import org.scalatest.MustMatchers.convertToAnyMustWrapper
import doobie.postgres.implicits._

import java.util.UUID

class BookwormIntegrationTest extends IntegrationTestModule {

  "Retrieving books" when {
    "Calling /books" should {
      "retrieve all books" in {
        val bookId = BookId(UUID.randomUUID())
        val authorId1 = AuthorId(UUID.randomUUID())
        val authorId2 = AuthorId(UUID.randomUUID())
        insertBooksAndAuthors(
          Map(
            BookEntity(bookId.id, "Harry Potter", "Awesome summary", "isbn123") -> List(
              AuthorEntity(authorId1.id, "John", "Black"),
              AuthorEntity(authorId2.id, "Peter", "White")
            )
          )
        )

        val expectedBooks = List(
          BookResponseDto(
            bookId.id,
            "Harry Potter",
            "Awesome summary",
            "isbn123",
            List(
              AuthorResponseDto(authorId1.id, "John", "Black", List.empty),
              AuthorResponseDto(authorId2.id, "Peter", "White", List.empty)
            )
          )
        )
        val request = Request[IO](Method.GET, uri"/books")
        val actualBooks = injector.getInstance(classOf[BookRestApi]).getAllBooks.orNotFound(request).unsafeRunSync()
        actualBooks.status mustBe Status.Ok
        actualBooks.as[List[BookResponseDto]].unsafeRunSync() mustBe expectedBooks
      }
    }
  }

  private def insertBooksAndAuthors(authorsByBook: Map[BookEntity, List[AuthorEntity]]): Unit = {
    def insertBookEntities(bookEntities: List[BookEntity]): ConnectionIO[Int] = {
      val sqlStatement = "insert into bookworm.book(bookId,title,summary,isbn) values (?, ?, ?, ?)"
      Update[BookEntity](sqlStatement).updateMany(bookEntities)
    }

    def insertAuthorEntities(authorEntities: List[AuthorEntity]): ConnectionIO[Int] = {
      val sqlStatement = "insert into bookworm.author(authorId,firstName,lastName) values (?,?,?)"
      Update[AuthorEntity](sqlStatement).updateMany(authorEntities)
    }

    def insertBookAuthorEntities(bookAuthorEntities: List[BookAuthorEntity]): ConnectionIO[Int] = {
      val sqlStatement = "insert into bookworm.book_author(bookId,authorId) values (?,?)"

      Update[(UUID, UUID)](sqlStatement).updateMany(
        bookAuthorEntities.map(bookAuthorEntity =>
          (bookAuthorEntity.bookEntity.bookId, bookAuthorEntity.authorEntity.authorId)
        )
      )
    }

    authorsByBook.foreach { entry =>
      val transaction = for {
        _ <- insertBookEntities(List(entry._1))
        _ <- insertAuthorEntities(entry._2)
        _ <- insertBookAuthorEntities(entry._2.map(authorEntity => BookAuthorEntity(entry._1, authorEntity)))
      } yield ()

      transaction.transact(synchronousTransactor).unsafeRunSync()
    }
  }
}
