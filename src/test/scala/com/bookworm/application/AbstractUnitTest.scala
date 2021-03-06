package com.bookworm.application

import com.bookworm.application.books.domain.model._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import java.util.UUID

abstract class AbstractUnitTest extends WordSpec with Matchers with MockFactory {

  val testBookTitle = BookTitle.create("title").toOption.get
  val testBookSummary = BookSummary.create("summary").toOption.get
  val testBookGenreId = GenreId(UUID.randomUUID())
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
}
