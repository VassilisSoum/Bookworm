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

      runInTransaction(bookRepository.add(testBook)) mustBe testBook
    }

    "Retrieve previous added book" in {
      val addedBook: BookQueryModel = runInTransaction(bookRepository.getById(testBookId)).get
      addedBook.bookId mustBe testBookId.id
      addedBook.genre mustBe testGenreName
      addedBook.id must be > 0L
      addedBook.isbn mustBe testBookIsbn.value
      addedBook.summary mustBe testBookSummary.value
      addedBook.title mustBe testBookTitle.value
      addedBook.updatedAt mustBe LocalDateTime.ofInstant(fakeClock.current, fakeClock.zoneId)
      addedBook.minPrice mustBe testBookMinPrice.value
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
        runInTransaction(bookRepository.update(expectedBook))

      response mustBe expectedBook
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
        runInTransaction(bookRepository.update(expectedBook))

      response mustBe expectedBook
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
        runInTransaction(bookRepository.update(expectedBook))

      response mustBe expectedBook
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

      val response = runInTransaction(bookRepository.update(expectedBook))

      response mustBe expectedBook
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

      val response = runInTransaction(bookRepository.update(expectedBook))

      response mustBe expectedBook
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

      val response = runInTransaction(bookRepository.update(expectedBook))

      response mustBe expectedBook
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

      val response = runInTransaction(bookRepository.update(expectedBook))

      response mustBe expectedBook
    }

    "Remove an existing book" in {
      runInTransaction(bookRepository.remove(testBookId)) mustBe ()
    }
  }
}
