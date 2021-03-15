package com.bookworm.application

import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.{AuthorQueryModel, BookQueryModel}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import java.time.LocalDateTime
import java.util.UUID

abstract class AbstractUnitTest extends WordSpec with Matchers with MockFactory {

  val testBookTitle = BookTitle.create("title").toOption.get
  val testBookSummary = BookSummary.create("summary").toOption.get
  val testBookGenreId = GenreId(UUID.randomUUID())
  val testBookGenreName = GenreName.create("Genre").toOption.get
  val testBookGenre = Genre(testBookGenreId, testBookGenreName)
  val testBookAuthors = List(AuthorId(UUID.randomUUID()))
  val testBookIsbn = BookIsbn.create("9781234567897").toOption.get
  val testBookMinPrice = BookPrice.create(1000L).toOption.get
  val testBookMaxPrice = BookPrice.create(5000L).toOption.get

  val testBook: Book = Book(
    bookId = BookId(UUID.randomUUID()),
    bookDetails = BookDetails
      .create(
        title = testBookTitle,
        summary = testBookSummary,
        isbn = testBookIsbn,
        genre = testBookGenreId,
        authors = testBookAuthors,
        minPrice = testBookMinPrice,
        maxPrice = testBookMaxPrice
      )
      .toOption
      .get,
    bookStatus = BookStatus.Available
  )

  val testBookQueryModel = BookQueryModel(
    bookId = testBook.bookId.id,
    title = testBookTitle.value,
    summary = testBookSummary.value,
    isbn = testBookIsbn.value,
    genre = testBookGenreName.genre,
    minPrice = testBookMinPrice.value,
    maxPrice = testBookMaxPrice.value,
    updatedAt = LocalDateTime.now(),
    id = 1L
  )

  val testAuthorId = AuthorId(UUID.randomUUID())
  val testAuthorFirstName = AuthorFirstName.create("Bill").toOption.get
  val testAuthorLastName = AuthorLastName.create("Soumakis").toOption.get

  val testAuthorQueryModel =
    AuthorQueryModel(
      authorId = testAuthorId.id,
      firstName = testAuthorFirstName.firstName,
      lastName = testAuthorLastName.lastName
    )
}
