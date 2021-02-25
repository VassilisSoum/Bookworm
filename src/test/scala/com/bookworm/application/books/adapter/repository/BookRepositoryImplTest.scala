package com.bookworm.application.books.adapter.repository

import cats.implicits.catsSyntaxApply
import com.bookworm.application.books.domain.model.{AuthorId, BookId, BookIsbn, BookSummary, BookTitle, DomainBusinessError, Genre, GenreId, GenreName}
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
    }

    "Return BookDoesNotExist when retrieving a book that does not exist" in {
      val expectedError = DomainBusinessError.BookDoesNotExist
      val actualError = bookRepository.getById(BookId(UUID.randomUUID())).unsafeRunSync().left.toOption.get

      actualError mustBe expectedError
    }

    "Update the title of an existing book" in {
      val bookTitle = BookTitle.create("Updated").toOption.get
      val expectedBook = testBook.copy(bookDetails = testBook.bookDetails.copy(title = bookTitle))
      val response =
        bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the summary of an existing book" in {
      val bookSummary = BookSummary.create("Updated").toOption.get
      val expectedBook = testBook.copy(bookDetails = testBook.bookDetails.copy(summary = bookSummary))
      val response =
        bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the isbn of an existing book" in {
      val bookIsbn = BookIsbn.create("0000000000000").toOption.get
      val expectedBook = testBook.copy(bookDetails = testBook.bookDetails.copy(isbn = bookIsbn))
      val response =
        bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the genre id of an existing book" in {
      val genreId = GenreId(UUID.randomUUID())
      runInTransaction(insertIntoGenre(Genre(genreId, GenreName.create("New Genre").toOption.get)))
      val expectedBook = testBook.copy(bookDetails = testBook.bookDetails.copy(genre = genreId))

      val response = bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Update the authors of an existing book" in {
      val authorId = AuthorId(UUID.randomUUID())
      runInTransaction(insertIntoAuthor(testAuthor.copy(authorId = authorId)))
      val expectedBook = testBook.copy(bookDetails = testBook.bookDetails.copy(authors = List(authorId)))

      val response = bookRepository.update(expectedBook).unsafeRunSync()

      response.isRight mustBe true
      response.toOption.get mustBe expectedBook
    }

    "Return OneOrMoreAuthorsDoNotExist when updating an existing book with at least one author that does not exist" in {
      val newAuthorId = AuthorId(UUID.randomUUID())
      val expectedBook =
        testBook.copy(bookDetails = testBook.bookDetails.copy(authors = List(newAuthorId, testAuthorId)))

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
