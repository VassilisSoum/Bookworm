package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.{BusinessErrorDto, GetAuthorsByBookIdResponseDto}
import com.bookworm.application.books.domain.model.DomainBusinessError
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.MustMatchers.convertToAnyMustWrapper

import java.time.LocalDateTime
import java.util.UUID

class RetrieveAuthorsByBookIdIntegrationTest extends TestData with AuthorEndpoints with EntityDecoders {

  override def beforeAll(): Unit = {
    setupInitialData()
    fakeClock.current = LocalDateTime
      .of(2025, 2, 7, 10, 0, 0)
      .atZone(fakeClock.zoneId)
      .toInstant
    super.beforeAll()
  }

  "Retrieving authors by book id" when {
    "Calling /authors/book/{bookId}" should {
      "return 200 OK and retrieve all authors of the specified book id" in {
        val request = Request[IO](Method.GET, Uri.unsafeFromString(s"/authors/book/${testBookId.id.toString}"))
        val response = endpoint(request)
          .unsafeRunSync()
        response.status mustBe Status.Ok

        val getAuthorsByBookIdResponseDto = response.as[GetAuthorsByBookIdResponseDto].unsafeRunSync()

        getAuthorsByBookIdResponseDto.authors.size mustBe 1
        getAuthorsByBookIdResponseDto.authors.head.authorId mustBe testAuthorId.id.toString
        getAuthorsByBookIdResponseDto.authors.head.firstName mustBe testAuthorFirstName.firstName
        getAuthorsByBookIdResponseDto.authors.head.lastName mustBe testAuthorLastName.lastName
      }

      "return 404 NOT_FOUND with error type BookDoesNotExist " +
        "when retrieving all authors of a book that does not exist" in {
        val request = Request[IO](Method.GET, Uri.unsafeFromString(s"/authors/book/${UUID.randomUUID().toString}"))
        val response = endpoint(request)
          .unsafeRunSync()
        response.status mustBe Status.NotFound

        val businessErrorDto = response.as[BusinessErrorDto].unsafeRunSync()

        businessErrorDto.errorType mustBe DomainBusinessError.BookDoesNotExist
      }
    }
  }

}
