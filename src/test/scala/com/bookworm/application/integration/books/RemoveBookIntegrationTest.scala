package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.BusinessErrorDto
import com.bookworm.application.books.domain.model.DomainBusinessError
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.MustMatchers.convertToAnyMustWrapper

class RemoveBookIntegrationTest extends TestData with BookEndpoints with EntityDecoders {

  "Remove a book" should {
    "return 204 NO CONTENT when the book referenced by path parameter exists" in {
      setupInitialData()
      val removeBookRequest = Request[IO](Method.DELETE, Uri.unsafeFromString(s"/books/${testBookId.id}"))

      val removeBookResponse = endpoint(removeBookRequest).unsafeRunSync()

      removeBookResponse.status mustBe Status.NoContent
    }

    "return 404 NotFound with errorType BookDoesNotExist when trying to remove a book that does not exist" in {
      val removeBookRequest = Request[IO](Method.DELETE, Uri.unsafeFromString(s"/books/${testBookId.id}"))

      val removeBookResponse = endpoint(removeBookRequest).unsafeRunSync()

      removeBookResponse.status mustBe Status.NotFound
      removeBookResponse.as[BusinessErrorDto].unsafeRunSync().errorType mustBe DomainBusinessError.BookDoesNotExist
    }
  }

}
