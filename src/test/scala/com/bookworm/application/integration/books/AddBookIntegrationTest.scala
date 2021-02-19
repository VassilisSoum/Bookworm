package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.{BusinessErrorDto, AddBookRequestDto, GetBooksResponseDto, ValidationErrorDto}
import com.bookworm.application.books.domain.model.{BusinessError, ValidationError}
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.MustMatchers.convertToAnyMustWrapper

import java.time.LocalDateTime
import java.util.UUID

class AddBookIntegrationTest extends TestData with BookEndpoints with EntityEncoders with EntityDecoders {

  "Adding a book" should {
    val createBookRequestDto = AddBookRequestDto(
      testBookTitle.value,
      testBookSummary.value,
      testBookIsbn.value,
      testGenreId.id.toString,
      List(testAuthorId.id.toString)
    )
    "return 204 NO CONTENT and add the book" in {
      fakeClock.current = LocalDateTime
        .of(2025, 2, 7, 10, 0, 0)
        .atZone(fakeClock.zoneId)
        .toInstant

      runInTransaction(insertIntoGenre(testGenre).flatMap(_ => insertIntoAuthor(testAuthor)))

      val addBookRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/books")).withEntity(createBookRequestDto)
      val addBookResponse = endpoint(addBookRequest)
        .unsafeRunSync()
      addBookResponse.status mustBe Status.NoContent

      val retrieveAllBooksRequest =
        Request[IO](Method.GET, Uri.unsafeFromString(s"/genre/${testGenreId.id.toString}/books"))
      val retrieveAllBooksResponse = endpoint(retrieveAllBooksRequest)
        .unsafeRunSync()
      retrieveAllBooksResponse.status mustBe Status.Ok

      val addedBook = retrieveAllBooksResponse.as[GetBooksResponseDto].unsafeRunSync().books.head

      addedBook.genre mustBe testGenre.genreName.genre
      addedBook.isbn mustBe testBookIsbn.value
      addedBook.title mustBe testBookTitle.value
      addedBook.summary mustBe testBookSummary.value
      addedBook.bookId mustNot be(empty)
    }

    "return 400 BAD REQUEST with error type EmptyBookTitle when the title of the book is empty" in {
      val request =
        Request[IO](Method.POST, Uri.unsafeFromString(s"/books")).withEntity(createBookRequestDto.copy(title = ""))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe ValidationError.EmptyBookTitle
    }

    "return 400 BAD REQUEST with error type EmptyBookSummary when the summary of the book is empty" in {
      val request =
        Request[IO](Method.POST, Uri.unsafeFromString(s"/books")).withEntity(createBookRequestDto.copy(summary = ""))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe ValidationError.EmptyBookSummary
    }

    "return 400 BAD REQUEST with error type EmptyBookIsbn when the isbn of the book is empty" in {
      val request =
        Request[IO](Method.POST, Uri.unsafeFromString(s"/books")).withEntity(createBookRequestDto.copy(isbn = ""))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe ValidationError.EmptyBookIsbn
    }

    "return 400 BAD REQUEST with error type InvalidIsbnLength when the isbn of the book is not 13 characters" in {
      val request =
        Request[IO](Method.POST, Uri.unsafeFromString(s"/books")).withEntity(createBookRequestDto.copy(isbn = "123"))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe ValidationError.InvalidIsbnLength
    }

    "return 400 BAD REQUEST with error type EmptyBookAuthorList when the provided list of authors field in the request is empty" in {
      val request =
        Request[IO](Method.POST, Uri.unsafeFromString(s"/books"))
          .withEntity(createBookRequestDto.copy(authorIds = List.empty))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe ValidationError.EmptyBookAuthorList
    }

    "return 400 BAD REQUEST with error type InvalidBookGenre when the provided genre id is not a uuid" in {
      val request =
        Request[IO](Method.POST, Uri.unsafeFromString(s"/books"))
          .withEntity(createBookRequestDto.copy(genreId = "unknown"))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe ValidationError.InvalidBookGenre
    }

    "return 409 CONFLICT with error type OneOrMoreAuthorsDoNotExist when one or more authors do not exist" in {
      val request =
        Request[IO](Method.POST, Uri.unsafeFromString(s"/books"))
          .withEntity(createBookRequestDto.copy(authorIds = List(testAuthorId.id.toString, UUID.randomUUID().toString)))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.Conflict
      response.as[BusinessErrorDto].unsafeRunSync().errorType mustBe BusinessError.OneOrMoreAuthorsDoNotExist
    }
  }
}
