package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.{BookResponseDto, GetBooksResponseDto}
import com.bookworm.application.books.domain.model.BookId
import org.http4s._
import org.scalatest.MustMatchers.convertToAnyMustWrapper

import java.time.LocalDateTime
import java.util.UUID

class RetrieveBooksWithPaginationIntegrationTest extends TestData with BookEndpoints with EntityDecoders {

  val bookId1: UUID = UUID.randomUUID()
  val bookId2: UUID = UUID.randomUUID()
  val bookId3: UUID = UUID.randomUUID()
  val bookId4: UUID = UUID.randomUUID()
  val bookId5: UUID = UUID.randomUUID()

  var continuationTokenInstantForFirstRun: LocalDateTime = _
  var continuationTokenInstantForSecondRun: LocalDateTime = _
  var lastBookIncrementalIdForFirstRun: Long = _
  var lastBookIncrementalIdForSecondRun: Long = _

  override def beforeAll(): Unit = {
    runInTransaction(insertIntoGenre(testGenre))
    continuationTokenInstantForSecondRun = LocalDateTime.ofInstant(fakeClock.current, fakeClock.zoneId)
    lastBookIncrementalIdForSecondRun = runInTransaction(insertIntoBook(testBook.copy(BookId(bookId1))))
    continuationTokenInstantForFirstRun = LocalDateTime.ofInstant(advanceClockInMillis(100L), fakeClock.zoneId)
    lastBookIncrementalIdForFirstRun = runInTransaction(insertIntoBook(testBook.copy(BookId(bookId2))))
    advanceClockInMillis(100L)
    runInTransaction(insertIntoBook(testBook.copy(BookId(bookId3))))
    advanceClockInMillis(100L)
    runInTransaction(insertIntoBook(testBook.copy(BookId(bookId4))))
    advanceClockInMillis(100L)
    runInTransaction(insertIntoBook(testBook.copy(BookId(bookId5))))
    super.beforeAll()
  }

  "retrieve books for the specified genre with pagination in descending order by default" should {
    val now = LocalDateTime
      .of(2025, 2, 7, 10, 0, 0)
      .atZone(fakeClock.zoneId)
      .toInstant
    setClockAt(now)

    "on the first retrieve 4 books out of 5 total in descending order" in {
      val expectedBooksForFirstRun = List(
        BookResponseDto(
          bookId = bookId5.toString,
          title = testBookTitle.value,
          summary = testBookSummary.value,
          isbn = testBookIsbn.value,
          genre = testGenre.genreName.genre
        ),
        BookResponseDto(
          bookId = bookId4.toString,
          title = testBookTitle.value,
          summary = testBookSummary.value,
          isbn = testBookIsbn.value,
          genre = testGenre.genreName.genre
        ),
        BookResponseDto(
          bookId = bookId3.toString,
          title = testBookTitle.value,
          summary = testBookSummary.value,
          isbn = testBookIsbn.value,
          genre = testGenre.genreName.genre
        ),
        BookResponseDto(
          bookId = bookId2.toString,
          title = testBookTitle.value,
          summary = testBookSummary.value,
          isbn = testBookIsbn.value,
          genre = testGenre.genreName.genre
        )
      )

      val expectedContinuationTokenForFirstRun =
        s"${continuationTokenInstantForFirstRun.toString}_$lastBookIncrementalIdForFirstRun"

      val expectedResponseForFirstRun = GetBooksResponseDto(
        expectedBooksForFirstRun,
        Some(expectedContinuationTokenForFirstRun)
      )
      val request =
        Request[IO](Method.GET, Uri.unsafeFromString(s"/genre/${testGenreId.id.toString}/books?limit=4"))
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.Ok

      response.as[GetBooksResponseDto].unsafeRunSync() mustBe expectedResponseForFirstRun
    }

    "on the second run use the continuation token returned in the previous run and retrieve the last item" in {
      val expectedBooksForSecondRun = List(
        BookResponseDto(
          bookId = bookId1.toString,
          title = testBookTitle.value,
          summary = testBookSummary.value,
          isbn = testBookIsbn.value,
          genre = testGenre.genreName.genre
        )
      )

      val continuationTokenForFirstRun =
        s"${continuationTokenInstantForFirstRun.toString}_$lastBookIncrementalIdForFirstRun"

      val expectedContinuationTokenForSecondRun =
        s"${continuationTokenInstantForSecondRun.toString}_$lastBookIncrementalIdForSecondRun"

      val expectedResponseForSecondRun = GetBooksResponseDto(
        expectedBooksForSecondRun,
        Some(expectedContinuationTokenForSecondRun)
      )
      val request = Request[IO](
        Method.GET,
        Uri.unsafeFromString(
          s"/genre/${testGenreId.id.toString}/books?limit=4&continuationToken=$continuationTokenForFirstRun"
        )
      )
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.Ok

      response.as[GetBooksResponseDto].unsafeRunSync() mustBe expectedResponseForSecondRun
    }

    "on the third run using continuation token of the previous run, no books should be returned" in {
      val expectedResponseForThirdRun = GetBooksResponseDto(
        List.empty,
        None
      )
      val continuationTokenForSecondRun =
        s"${continuationTokenInstantForSecondRun.toString}_$lastBookIncrementalIdForSecondRun"
      val request = Request[IO](
        Method.GET,
        Uri.unsafeFromString(
          s"/genre/${testGenreId.id.toString}/books?limit=4&continuationToken=$continuationTokenForSecondRun"
        )
      )
      val response = endpoint(request)
        .unsafeRunSync()
      response.status mustBe Status.Ok

      response.as[GetBooksResponseDto].unsafeRunSync() mustBe expectedResponseForThirdRun
    }
  }
}
