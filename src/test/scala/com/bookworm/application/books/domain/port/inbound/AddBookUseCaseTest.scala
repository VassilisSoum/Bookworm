package com.bookworm.application.books.domain.port.inbound

import cats.Id
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.books.domain.model.DomainBusinessError
import com.bookworm.application.books.domain.port.outbound.{AuthorRepository, BookRepository}

class AddBookUseCaseTest extends AbstractUnitTest {

  val bookRepository: BookRepository[Id] = mock[BookRepository[Id]]
  val authorRepository: AuthorRepository[Id] = mock[AuthorRepository[Id]]
  val addBookUseCase = new AddBookUseCase[Id](bookRepository, authorRepository)

  "AddBookUseCase" should {
    "add a new book" in {
      (authorRepository.exist _).expects(testBookAuthors).returns(true)
      (bookRepository.add _).expects(testBook).returns(testBook)

      addBookUseCase.addBook(testBook) shouldBe Right(testBook)
    }

    "return OneOrMoreAuthorsDoNotExist" in {
      (authorRepository.exist _).expects(testBookAuthors).returns(false)
      (bookRepository.add _).expects(*).never()

      addBookUseCase.addBook(testBook) shouldBe Left(DomainBusinessError.OneOrMoreAuthorsDoNotExist)
    }
  }
}
