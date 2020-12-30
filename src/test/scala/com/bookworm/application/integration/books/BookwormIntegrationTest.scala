package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.IntegrationTestModule
import com.bookworm.application.books.repository.model.Book
import com.bookworm.application.books.rest.BookRestApi
import com.bookworm.application.books.rest.dto.BookDto
import doobie.implicits._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
import org.http4s.{Method, Request, Status}
import org.scalatest.MustMatchers.convertToAnyMustWrapper

class BookwormIntegrationTest extends IntegrationTestModule {

  "Retrieving books" when {
    "Calling /books" should {
      "retrieve all books" in {
        insertBookInDB(Book(1L, "Harry Potter", "Awesome summary"))

        val expectedBooks = List(BookDto(1L, "Harry Potter", "Awesome summary"))
        val request = Request[IO](Method.GET, uri"/books")
        val actualBooks = injector.getInstance(classOf[BookRestApi]).getAllBooks.orNotFound(request).unsafeRunSync()
        actualBooks.status mustBe Status.Ok
        actualBooks.as[List[BookDto]].unsafeRunSync() mustBe expectedBooks
      }
    }
  }

  private def insertBookInDB(book: Book): Unit = {
    sql"insert into bookworm.book(title,summary) values (${book.title}, ${book.summary})".update.run
      .transact(synchronousTransactor)
      .unsafeRunSync()
    ()
  }
}
