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
    "return all books of a specific genre" in {
      val genre = UUID.randomUUID()
      val expectedAuthors = List(
        Author(AuthorId(UUID.randomUUID()), "John", "Black"),
        Author(AuthorId(UUID.randomUUID()), "Peter", "White")
      )
      val expectedBooks = Map(
        Book(
          bookId = BookId(UUID.randomUUID()),
          title = "Foo",
          summary = "Bar",
          isbn = "123"
        ) -> expectedAuthors
      )

      (bookRepository.getBooksAndAuthorsForGenre _).expects(genre).returns(IO(expectedBooks))

      val actualBooks = bookService.retrieveAllBooks(genre)

      actualBooks.unsafeRunSync() mustBe expectedBooks
    }
  }
}
