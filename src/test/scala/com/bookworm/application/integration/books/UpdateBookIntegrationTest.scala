package com.bookworm.application.integration.books

import cats.effect.IO
import cats.implicits.catsSyntaxApply
import com.bookworm.application.books.adapter.api.dto.{BusinessErrorDto, GetBooksResponseDto, UpdateBookRequestDto, ValidationErrorDto}
import com.bookworm.application.books.domain.model.{AuthorId, DomainBusinessError, DomainValidationError, GenreId, GenreName}
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.MustMatchers.convertToAnyMustWrapper

import java.time.LocalDateTime
import java.util.UUID

class UpdateBookIntegrationTest extends TestData with BookEndpoints with EntityEncoders with EntityDecoders {

  "Updating a book" should {
    val newGenreId = UUID.randomUUID()
    val newGenreName = "New Genre"
    val newAuthorIds = List(UUID.randomUUID())

    val updateBookRequestDto =
      UpdateBookRequestDto(
        title = "New title",
        summary = "New summary",
        isbn = "0000000000000",
        genreId = newGenreId.toString,
        authorIds = newAuthorIds.map(_.toString),
        minPrice = 0L,
        maxPrice = 1000L
      )
    "return 204 NO CONTENT when the update was successful" in {
      fakeClock.current = LocalDateTime
        .of(2025, 2, 7, 10, 0, 0)
        .atZone(fakeClock.zoneId)
        .toInstant

      setupInitialData()

      runInTransaction(
        insertIntoGenre(
          testGenre.copy(genreId = GenreId(newGenreId), genreName = GenreName.create(newGenreName).toOption.get)
        ) *> insertIntoAuthor(
          testAuthor.copy(authorId = AuthorId(newAuthorIds.head))
        )
      )

      val updateBookRequest =
        Request[IO](Method.PUT, Uri.unsafeFromString(s"/books/${testBookId.id.toString}"))
          .withEntity(updateBookRequestDto)
      val updateBookResponse = endpoint(updateBookRequest)
        .unsafeRunSync()
      updateBookResponse.status mustBe Status.NoContent

      val retrieveAllBooksRequest =
        Request[IO](Method.GET, Uri.unsafeFromString(s"/genre/${newGenreId.toString}/books"))
      val retrieveAllBooksResponse = endpoint(retrieveAllBooksRequest)
        .unsafeRunSync()
      retrieveAllBooksResponse.status mustBe Status.Ok

      val addedBook = retrieveAllBooksResponse.as[GetBooksResponseDto].unsafeRunSync().books.head

      addedBook.genre mustBe newGenreName
      addedBook.isbn mustBe updateBookRequestDto.isbn
      addedBook.title mustBe updateBookRequestDto.title
      addedBook.summary mustBe updateBookRequestDto.summary
      addedBook.bookId mustBe testBookId.id.toString
      addedBook.minPrice mustBe updateBookRequestDto.minPrice
      addedBook.maxPrice mustBe updateBookRequestDto.maxPrice
    }
    "return 400 BAD REQUEST with error type EmptyBookTitle when the title of the book is empty" in {
      val request =
        Request[IO](Method.PUT, Uri.unsafeFromString(s"/books/${testBookId.id.toString}"))
          .withEntity(updateBookRequestDto.copy(title = ""))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe DomainValidationError.EmptyBookTitle
    }

    "return 400 BAD REQUEST with error type EmptyBookSummary when the summary of the book is empty" in {
      val request =
        Request[IO](Method.PUT, Uri.unsafeFromString(s"/books/${testBookId.id.toString}"))
          .withEntity(updateBookRequestDto.copy(summary = ""))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe DomainValidationError.EmptyBookSummary
    }

    "return 400 BAD REQUEST with error type EmptyBookIsbn when the isbn of the book is empty" in {
      val request =
        Request[IO](Method.PUT, Uri.unsafeFromString(s"/books/${testBookId.id.toString}"))
          .withEntity(updateBookRequestDto.copy(isbn = ""))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe DomainValidationError.EmptyBookIsbn
    }

    "return 400 BAD REQUEST with error type InvalidIsbnLength when the isbn of the book is not 13 characters" in {
      val request =
        Request[IO](Method.PUT, Uri.unsafeFromString(s"/books/${testBookId.id.toString}"))
          .withEntity(updateBookRequestDto.copy(isbn = "123"))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe DomainValidationError.InvalidIsbnLength
    }

    "return 400 BAD REQUEST with error type EmptyBookAuthorList when the provided list of authors field in the request is empty" in {
      val request =
        Request[IO](Method.PUT, Uri.unsafeFromString(s"/books/${testBookId.id.toString}"))
          .withEntity(updateBookRequestDto.copy(authorIds = List.empty))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe DomainValidationError.EmptyBookAuthorList
    }

    "return 400 BAD REQUEST with error type InvalidBookGenre when the provided genre id is not a uuid" in {
      val request =
        Request[IO](Method.PUT, Uri.unsafeFromString(s"/books/${testBookId.id.toString}"))
          .withEntity(updateBookRequestDto.copy(genreId = "unknown"))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.BadRequest
      response.as[ValidationErrorDto].unsafeRunSync().errorType mustBe DomainValidationError.InvalidBookGenre
    }

    "return 409 CONFLICT with error type OneOrMoreAuthorsDoNotExist when one or more authors do not exist" in {
      val request =
        Request[IO](Method.PUT, Uri.unsafeFromString(s"/books/${testBookId.id.toString}"))
          .withEntity(updateBookRequestDto.copy(authorIds = List(testAuthorId.id.toString, UUID.randomUUID().toString)))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.Conflict
      response.as[BusinessErrorDto].unsafeRunSync().errorType mustBe DomainBusinessError.OneOrMoreAuthorsDoNotExist
    }
  }
}
