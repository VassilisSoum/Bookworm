package com.bookworm.application.books.adapter.repository

import cats.implicits.catsSyntaxApply
import com.bookworm.application.books.domain.model._
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

      bookRepository.add(testBook).unsafeRunSync().toOption.get mustBe testBook
    }

    "Retrieve previous added book" in {
      val addedBook: BookQueryModel = bookRepository.getById(testBookId).unsafeRunSync().toOption.get
      addedBook.bookId mustBe testBookId.id
      addedBook.genre mustBe testGenreName
      addedBook.id must be > 0L
      addedBook.isbn mustBe testBookIsbn.value
      addedBook.summary mustBe testBookSummary.value
      addedBook.title mustBe testBookTitle.value
      addedBook.updatedAt mustBe LocalDateTime.ofInstant(fakeClock.current, fakeClock.zoneId)
      addedBook.minPrice mustBe testBookMinPrice.value
    }

    "Return BookDoesNotExist when retrieving a book that does not exist" in {
      val expectedError = DomainBusinessError.BookDoesNotExist
      val actualError = bookRepository.getById(BookId(UUID.randomUUID())).unsafeRunSync().left.toOption.get

      actualError mustBe expectedError
    }

    "Update the title of an existing book" in {
      val updatedBookTitle = BookTitle.create("Updated").toOption.get
      val expectedBook = testBook.copy(bookDetails =
        BookDetails
          .create(
            updatedBookTitle,
            testBookSummary,
            testBookIsbn,
            testGenreId,
            List(testAuthorId),
            testBookMinPrice,
            testBookMaxPrice
          )
          .toOption
          .get
      )
      val response =
        bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the summary of an existing book" in {
      val updatedBookSummary = BookSummary.create("Updated").toOption.get
      val expectedBook = testBook.copy(bookDetails =
        BookDetails
          .create(
            testBookTitle,
            updatedBookSummary,
            testBookIsbn,
            testGenreId,
            List(testAuthorId),
            testBookMinPrice,
            testBookMaxPrice
          )
          .toOption
          .get
      )
      val response =
        bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the isbn of an existing book" in {
      val updatedBookIsbn = BookIsbn.create("0000000000000").toOption.get
      val expectedBook = testBook.copy(bookDetails =
        BookDetails
          .create(
            testBookTitle,
            testBookSummary,
            updatedBookIsbn,
            testGenreId,
            List(testAuthorId),
            testBookMinPrice,
            testBookMaxPrice
          )
          .toOption
          .get
      )
      val response =
        bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the genre id of an existing book" in {
      val updatedGenreId = GenreId(UUID.randomUUID())
      runInTransaction(insertIntoGenre(Genre(updatedGenreId, GenreName.create("New Genre").toOption.get)))
      val expectedBook = testBook.copy(bookDetails =
        BookDetails
          .create(
            testBookTitle,
            testBookSummary,
            testBookIsbn,
            updatedGenreId,
            List(testAuthorId),
            testBookMinPrice,
            testBookMaxPrice
          )
          .toOption
          .get
      )

      val response = bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the authors of an existing book" in {
      val updatedAuthorId = AuthorId(UUID.randomUUID())
      runInTransaction(insertIntoAuthor(testAuthor.copy(authorId = updatedAuthorId)))
      val expectedBook = testBook.copy(bookDetails =
        BookDetails
          .create(
            testBookTitle,
            testBookSummary,
            testBookIsbn,
            testGenreId,
            List(updatedAuthorId),
            testBookMinPrice,
            testBookMaxPrice
          )
          .toOption
          .get
      )

      val response = bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the minPrice of an existing book" in {
      val updatedMinBookPrice = BookPrice.create(0L).toOption.get
      val expectedBook = testBook.copy(bookDetails =
        BookDetails
          .create(
            testBookTitle,
            testBookSummary,
            testBookIsbn,
            testGenreId,
            List(testAuthorId),
            updatedMinBookPrice,
            testBookMaxPrice
          )
          .toOption
          .get
      )

      val response = bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the maxPrice of an existing book" in {
      val updatedMaxBookPrice = BookPrice.create(testBookMinPrice.value + 1000L).toOption.get
      val expectedBook = testBook.copy(bookDetails =
        BookDetails
          .create(
            testBookTitle,
            testBookSummary,
            testBookIsbn,
            testGenreId,
            List(testAuthorId),
            testBookMinPrice,
            updatedMaxBookPrice
          )
          .toOption
          .get
      )

      val response = bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Return OneOrMoreAuthorsDoNotExist when updating an existing book with at least one author that does not exist" in {
      val newAuthorId = AuthorId(UUID.randomUUID())
      val expectedBook =
        testBook.copy(bookDetails =
          BookDetails
            .create(
              testBookTitle,
              testBookSummary,
              testBookIsbn,
              testGenreId,
              List(newAuthorId, testAuthorId),
              testBookMinPrice,
              testBookMaxPrice
            )
            .toOption
            .get
        )

      val response = bookRepository.update(expectedBook).unsafeRunSync()

      response.isLeft mustBe true
      response.left.toOption.get mustBe DomainBusinessError.OneOrMoreAuthorsDoNotExist
    }

    "Remove an existing book" in {
      bookRepository.remove(testBookId).unsafeRunSync().isRight mustBe true

      bookRepository.remove(testBookId).unsafeRunSync().isLeft mustBe true
    }
  }
}
