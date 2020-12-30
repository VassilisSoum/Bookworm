package com.bookworm.application.books.service

import cats.effect.IO
import com.bookworm.application.books.repository.BookRepository
import com.bookworm.application.books.repository.model.Book
import org.scalamock.scalatest.MockFactory
import org.scalatest.MustMatchers.convertToAnyMustWrapper
import org.scalatest.{Matchers, WordSpec}

class BookServiceSpec extends WordSpec with Matchers with MockFactory {

  val bookRepository: BookRepository = mock[BookRepository]
  val bookService: BookService = new BookServiceImpl(bookRepository)

  "BookService" should {
    "return all books" in {
      val expectedBooks = List(Book(1L, "Foo", "Bar"))

      (() => bookRepository.getBooks).expects().returns(IO(expectedBooks))

      val actualBooks = bookService.retrieveAllBooks

      actualBooks.unsafeRunSync() mustBe expectedBooks
    }
  }
}
