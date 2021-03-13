package com.bookworm.application.books.domain.port.inbound

import cats.Id
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.books.domain.model.DomainBusinessError
import com.bookworm.application.books.domain.port.outbound.{AuthorRepository, BookRepository}

class UpdateBookUseCaseTest extends AbstractUnitTest {
  val bookRepository: BookRepository[Id] = mock[BookRepository[Id]]
  val authorRepository: AuthorRepository[Id] = mock[AuthorRepository[Id]]

  val updateBookUseCase: UpdateBookUseCase[Id] = new UpdateBookUseCase[Id](bookRepository, authorRepository)

  "UpdateBookUseCase" should {
    "update a book" in {
      (authorRepository.exist _).expects(testBookAuthors).returns(true).once()
      (bookRepository.getById _).expects(testBook.bookId).returns(Some(testBookQueryModel)).once()
      (bookRepository.update _).expects(testBook).returns(testBook).once()

      updateBookUseCase.updateBook(testBook) shouldBe Right(testBook)
    }

    "return BookDoesNotExist" in {
      (authorRepository.exist _).expects(testBookAuthors).returns(true).once()
      (bookRepository.getById _).expects(testBook.bookId).returns(None).once()
      (bookRepository.update _).expects(*).never()

      updateBookUseCase.updateBook(testBook) shouldBe Left(DomainBusinessError.BookDoesNotExist)
    }

    "return OneOrMoreAuthorsDoNotExist" in {
      (authorRepository.exist _).expects(testBookAuthors).returns(false).once()
      (bookRepository.getById _).expects(*).returns(None).never()
      (bookRepository.update _).expects(*).never()

      updateBookUseCase.updateBook(testBook) shouldBe Left(DomainBusinessError.OneOrMoreAuthorsDoNotExist)
    }
  }
}
