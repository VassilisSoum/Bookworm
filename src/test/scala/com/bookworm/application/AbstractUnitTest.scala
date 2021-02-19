package com.bookworm.application

import com.bookworm.application.books.domain.model.{AuthorId, Book, BookDetails, BookId, BookIsbn, BookSummary, BookTitle, GenreId}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import java.util.UUID

abstract class AbstractUnitTest extends WordSpec with Matchers with MockFactory {
  val testBook = Book(
    BookId(UUID.randomUUID()),
    BookDetails(
      BookTitle.create("title").toOption.get,
      BookSummary.create("summary").toOption.get,
      BookIsbn.create("9781234567897").toOption.get,
      GenreId(UUID.randomUUID()),
      List(AuthorId(UUID.randomUUID()))
    )
  )
}
