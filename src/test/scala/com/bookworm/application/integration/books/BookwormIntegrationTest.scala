package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.books.adapter.api.BookRestApi
import com.bookworm.application.books.adapter.api.dto.BookAndAuthorResponseDto
import com.google.inject.Key
import net.codingwell.scalaguice
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.MustMatchers.convertToAnyMustWrapper

import java.util.UUID

class BookwormIntegrationTest extends TestData {

  val endpoint = injector.getInstance(Key.get(scalaguice.typeLiteral[BookRestApi[IO]])).getAllBooks /*<+>*/ .orNotFound

  "Retrieving books" when {
    "Calling /genre/{genreId}/books" should {
      "retrieve all books for the specified genre" in {
        val expectedBooks =
          Map(
            testBookId.id -> List(
              BookAndAuthorResponseDto(
                bookId = testBookId.id,
                title = testBookTitle.title,
                summary = testBookSummary.summary,
                isbn = testBookIsbn.isbn,
                genre = testGenre.genreName.genre,
                authorId = testAuthorId.id,
                firstName = testAuthorFirstName.firstName,
                lastName = testAuthorLastName.lastName
              )
            )
          )
        val request = Request[IO](Method.GET, Uri.unsafeFromString(s"/genre/${testGenreId.id.toString}/books"))
        val actualBooks = endpoint(request)
          .unsafeRunSync()
        actualBooks.status mustBe Status.Ok
        actualBooks.as[Map[UUID, List[BookAndAuthorResponseDto]]].unsafeRunSync() mustBe expectedBooks
      }
    }
  }
}
