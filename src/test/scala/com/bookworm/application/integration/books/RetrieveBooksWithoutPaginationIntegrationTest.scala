package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.{BookResponseDto, GetBooksResponseDto, ValidationErrorDto}
import com.bookworm.application.books.domain.model.ValidationError
import org.http4s._
import org.scalatest.MustMatchers.convertToAnyMustWrapper

import java.time.LocalDateTime

class RetrieveBooksWithoutPaginationIntegrationTest extends TestData with BookEndpoints with EntityDecoders {

  "Retrieving books" when {
    "Calling /genre/{genreId}/books" should {
      "retrieve all books for the specified genre without providing pagination info" in {
        fakeClock.current = LocalDateTime
          .of(2025, 2, 7, 10, 0, 0)
          .atZone(fakeClock.zoneId)
          .toInstant

        val bookIncrementalId = runInTransaction(insertIntoGenre(testGenre).flatMap(_ => insertIntoBook(testBook)))

        val expectedBooks = List(
          BookResponseDto(
            bookId = testBookId.id.toString,
            title = testBookTitle.value,
            summary = testBookSummary.value,
            isbn = testBookIsbn.value,
            genre = testGenre.genreName.genre
          )
        )
        val expectedResponse = GetBooksResponseDto(
          expectedBooks,
          Some(s"${LocalDateTime.ofInstant(fakeClock.current, fakeClock.zoneId).toString}_$bookIncrementalId")
        )
        val request = Request[IO](Method.GET, Uri.unsafeFromString(s"/genre/${testGenreId.id.toString}/books"))
        val response = endpoint(request)
          .unsafeRunSync()
        response.status mustBe Status.Ok

        response.as[GetBooksResponseDto].unsafeRunSync() mustBe expectedResponse
      }

      "return 400 BAD REQUEST with error type EmptyContinuationToken when trying to retrieve all books with empty continuation token as pagination info" in {
        val request =
          Request[IO](Method.GET, Uri.unsafeFromString(s"/genre/${testGenreId.id.toString}/books?continuationToken="))
        val actualBooks = endpoint(request)
          .unsafeRunSync()
        actualBooks.status mustBe Status.BadRequest
        actualBooks.as[ValidationErrorDto].unsafeRunSync().errorType mustBe ValidationError.EmptyContinuationToken
      }

      "return 400 BAD REQUEST with error type InvalidContinuationTokenFormat when trying to retrieve all books with continuation token which is invalid" in {
        val request = Request[IO](
          Method.GET,
          Uri.unsafeFromString(s"/genre/${testGenreId.id.toString}/books?continuationToken='29-01-2021T21:00:45'")
        )
        val actualBooks = endpoint(request)
          .unsafeRunSync()
        actualBooks.status mustBe Status.BadRequest
        actualBooks
          .as[ValidationErrorDto]
          .unsafeRunSync()
          .errorType mustBe ValidationError.InvalidContinuationTokenFormat
      }

      "return 400 BAD REQUEST with error type NonPositivePaginationLimit when trying to retrieve all books with a non-positive pagination limit" in {
        val request = Request[IO](
          Method.GET,
          Uri.unsafeFromString(
            s"/genre/${testGenreId.id.toString}/books?continuationToken='29-01-2021T21:00:45_2727'&limit=-10"
          )
        )
        val actualBooks = endpoint(request)
          .unsafeRunSync()
        actualBooks.status mustBe Status.BadRequest
        actualBooks
          .as[ValidationErrorDto]
          .unsafeRunSync()
          .errorType mustBe ValidationError.NonPositivePaginationLimit
      }

      "return 400 BAD REQUEST with error type PaginationLimitExceedsMaximum when trying to retrieve all books with too large pagination limit" in {
        val request = Request[IO](
          Method.GET,
          Uri.unsafeFromString(
            s"/genre/${testGenreId.id.toString}/books?continuationToken='29-01-2021T21:00:45_2727'&limit=1000000"
          )
        )
        val actualBooks = endpoint(request)
          .unsafeRunSync()
        actualBooks.status mustBe Status.BadRequest
        actualBooks
          .as[ValidationErrorDto]
          .unsafeRunSync()
          .errorType mustBe ValidationError.PaginationLimitExceedsMaximum
      }
    }
  }
}
