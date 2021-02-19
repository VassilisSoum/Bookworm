package com.bookworm.application.books.adapter.repository

import cats.implicits.catsSyntaxApply
import com.bookworm.application.books.domain.model.{BookId, BusinessError}
import com.bookworm.application.books.domain.port.inbound.query.BookQueryModel
import com.bookworm.application.integration.books.TestData
import org.scalatest.MustMatchers.convertToAnyMustWrapper

import java.time.LocalDateTime
import java.util.UUID

class BookRepositoryImplTest extends TestData {

  "BookRepositoryImpl" should {
    fakeClock.current = LocalDateTime
      .of(2025, 2, 7, 10, 0, 0)
      .atZone(fakeClock.zoneId)
      .toInstant
    val bookRepository = injector.getInstance(classOf[BookRepositoryImpl])
    "Add a new book" in {

      runInTransaction(insertIntoGenre(testGenre) *> insertIntoAuthor(testAuthor))

      bookRepository.addBook(testBook).unsafeRunSync().toOption.get mustBe testBook
    }

    "Retrieve previous added book" in {
      val addedBook: BookQueryModel = bookRepository.getBookById(testBookId).unsafeRunSync().toOption.get
      addedBook.bookId mustBe testBookId.id
      addedBook.genre mustBe testGenreName
      addedBook.id must be > 0L
      addedBook.isbn mustBe testBookIsbn.value
      addedBook.summary mustBe testBookSummary.value
      addedBook.title mustBe testBookTitle.value
      addedBook.updatedAt mustBe LocalDateTime.ofInstant(fakeClock.current, fakeClock.zoneId)
    }

    val expectedError = BusinessError.BookDoesNotExist
    s"Return $expectedError when retrieving a book that does not exist" in {
      val actualError = bookRepository.getBookById(BookId(UUID.randomUUID())).unsafeRunSync().left.toOption.get

      actualError mustBe expectedError
    }
  }
}
