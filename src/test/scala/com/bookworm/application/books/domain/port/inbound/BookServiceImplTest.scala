package com.bookworm.application.books.domain.port.inbound

import cats.effect.IO
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.{BookQueryModel, BooksByGenreQuery}
import com.bookworm.application.books.domain.port.outbound.BookRepository
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import java.time.LocalDateTime
import java.util.UUID

class BookServiceImplTest extends WordSpec with Matchers with MockFactory {

  val bookRepository: BookRepository[IO] = mock[BookRepository[IO]]
  val bookService: BookService[IO] = new BookServiceImpl(bookRepository)

  "BookService" should {
    "return books of a specific genre with continuation token given pagination information in descending order" in {
      val genreId = GenreId(UUID.randomUUID())
      val bookId1 = BookId(UUID.randomUUID())
      val bookId2 = BookId(UUID.randomUUID())
      val paginationInfo = createPaginationInfo
      val updatedTimestampOfFirstBook =
        LocalDateTime.of(2025, 10, 10, 17, 23, 37)
      val updatedTimestampOfSecondBook = updatedTimestampOfFirstBook.plusSeconds(10)
      val id1 = 1L
      val id2 = 2L
      val expectedBooks = List(
        BookQueryModel(
          bookId = bookId2.id,
          title = "Harry Potter",
          summary = "Awesome book",
          isbn = "ISBN123",
          genre = "Fantasy",
          updatedAt = updatedTimestampOfSecondBook,
          id = id2
        ),
        BookQueryModel(
          bookId = bookId1.id,
          title = "The Girl with the dragon tattoo",
          summary = "Awesome book",
          isbn = "ISBN1234546",
          genre = "Fantasy",
          updatedAt = updatedTimestampOfFirstBook,
          id = id1
        )
      )

      val booksByGenreQuery = BooksByGenreQuery(
        expectedBooks,
        Some(ContinuationToken.create(s"$updatedTimestampOfFirstBook${ContinuationToken.delimiter}$id1").toOption.get)
      )

      (bookRepository.getBooksForGenre _).expects(genreId, paginationInfo).returns(IO.pure(booksByGenreQuery))

      val actualBooks = bookService.retrieveBooksByGenre(genreId, paginationInfo).unsafeRunSync()

      actualBooks.continuationToken.get.continuationToken shouldBe s"$updatedTimestampOfFirstBook${ContinuationToken.delimiter}$id1"

      actualBooks.books.size shouldBe 2
      actualBooks.books shouldBe expectedBooks
    }

    "return empty map of books and no continuation token" in {
      val genreId = GenreId(UUID.randomUUID())
      val paginationInfo = createPaginationInfo

      val booksByGenreQuery = BooksByGenreQuery(List.empty, None)
      (bookRepository.getBooksForGenre _).expects(genreId, paginationInfo).returns(IO.pure(booksByGenreQuery))

      val actualBooks = bookService.retrieveBooksByGenre(genreId, paginationInfo).unsafeRunSync()

      actualBooks.continuationToken shouldBe None

      actualBooks.books.isEmpty shouldBe true

    }

    "create a book" in {
      val book = Book(
        BookId(UUID.randomUUID()),
        BookDetails(
          BookTitle.create("title").toOption.get,
          BookSummary.create("summary").toOption.get,
          BookIsbn.create("isbn").toOption.get,
          GenreId(UUID.randomUUID()),
          List(AuthorId(UUID.randomUUID()))
        )
      )

      (bookRepository.addBook _).expects(book).returns(IO.pure(Right(book)))

      val response = bookService.createBook(book).unsafeRunSync()

      response.isRight shouldBe true
    }

    "return BusinessError when creating a book" in {
      val book = Book(
        BookId(UUID.randomUUID()),
        BookDetails(
          BookTitle.create("title").toOption.get,
          BookSummary.create("summary").toOption.get,
          BookIsbn.create("isbn").toOption.get,
          GenreId(UUID.randomUUID()),
          List(AuthorId(UUID.randomUUID()))
        )
      )

      (bookRepository.addBook _).expects(book).returns(IO.pure(Left(BusinessError.OneOrMoreAuthorsDoNotExist)))

      val response = bookService.createBook(book).unsafeRunSync()

      response.isLeft shouldBe true
      response.left.toOption.get == BusinessError.OneOrMoreAuthorsDoNotExist
    }
  }

  private def createPaginationInfo: PaginationInfo = {
    val continuationToken = ContinuationToken.create("29-01-2021T21:00:45_148").toOption.get
    val limit = PaginationLimit.create(10).toOption.get

    PaginationInfo(Some(continuationToken), limit)
  }
}
