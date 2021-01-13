package com.bookworm.application.books.service

import cats.effect.IO
import com.bookworm.application.books.service.repository.BookRepository
import com.bookworm.application.books.service.repository.model.{Author, AuthorId, Book, BookId}
import org.scalamock.scalatest.MockFactory
import org.scalatest.MustMatchers.convertToAnyMustWrapper
import org.scalatest.{Matchers, WordSpec}

import java.util.UUID

class BookServiceSpec extends WordSpec with Matchers with MockFactory {

  val bookRepository: BookRepository = mock[BookRepository]
  val bookService: BookService = new BookServiceImpl(bookRepository)

  "BookService" should {
    "return all books" in {
      val expectedAuthors = List(
        Author(AuthorId(UUID.randomUUID()), "John", "Black", List.empty),
        Author(AuthorId(UUID.randomUUID()), "Peter", "White", List.empty)
      )
      val expectedBooks = List(
        Book(
          bookId = BookId(UUID.randomUUID()),
          title = "Foo",
          summary = "Bar",
          authors = expectedAuthors,
          isbn = "123"
        )
      )

      (() => bookRepository.getBooks).expects().returns(IO(expectedBooks))

      val actualBooks = bookService.retrieveAllBooks

      actualBooks.unsafeRunSync() mustBe expectedBooks
    }
  }
}
