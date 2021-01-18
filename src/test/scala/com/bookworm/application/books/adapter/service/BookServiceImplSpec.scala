package com.bookworm.application.books.adapter.service

import cats.effect.IO
import com.bookworm.application.books.domain.model.{BookId, GenreId}
import com.bookworm.application.books.domain.port.inbound.BookService
import com.bookworm.application.books.domain.port.inbound.query.BookWithAuthorQuery
import com.bookworm.application.books.domain.port.outbound.BookRepository
import org.scalamock.scalatest.MockFactory
import org.scalatest.MustMatchers.convertToAnyMustWrapper
import org.scalatest.{Matchers, WordSpec}

import java.util.UUID

class BookServiceImplSpec extends WordSpec with Matchers with MockFactory {

  val bookRepository: BookRepository[IO] = mock[BookRepository[IO]]
  val bookService: BookService[IO] = new BookServiceImpl(bookRepository)

  "BookService" should {
    "return all books of a specific genre" in {
      val genreId = GenreId(UUID.randomUUID())
      val bookId = BookId(UUID.randomUUID())
      val expectedBooks = Map(
        bookId -> List(
          BookWithAuthorQuery(
            bookId = bookId.id,
            title = "Harry Potter",
            summary = "Awesome book",
            isbn = "ISBN123",
            genre = "Fantasy",
            authorId = UUID.randomUUID(),
            firstName = "John",
            lastName = "Black"
          )
        )
      )

      (bookRepository.getBooksAndAuthorsForGenre _).expects(genreId).returns(IO.pure(expectedBooks))

      val actualBooks = bookService.retrieveAllBooksByGenre(genreId)

      actualBooks.unsafeRunSync() mustBe expectedBooks
    }
  }
}
