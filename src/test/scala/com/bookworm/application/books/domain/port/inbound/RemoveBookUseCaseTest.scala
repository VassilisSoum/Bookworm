package com.bookworm.application.books.domain.port.inbound

import cats.Id
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.books.domain.model.DomainBusinessError
import com.bookworm.application.books.domain.port.outbound.BookRepository

class RemoveBookUseCaseTest extends AbstractUnitTest {
  val bookRepository: BookRepository[Id] = mock[BookRepository[Id]]
  val removeBookUseCase: RemoveBookUseCase[Id] = new RemoveBookUseCase[Id](bookRepository)

  "RemoveBookUseCase" should {
    "remove a book that exists" in {
      (bookRepository.getById _).expects(testBook.bookId).returns(Some(testBookQueryModel)).once()
      (bookRepository.remove _).expects(testBook.bookId).returns(()).once()

      removeBookUseCase.removeBook(testBook.bookId) shouldBe Right(())
    }

    "return BookDoesNotExist" in {
      (bookRepository.getById _).expects(testBook.bookId).returns(None).once()
      (bookRepository.remove _).expects(*).never()

      removeBookUseCase.removeBook(testBook.bookId) shouldBe Left(DomainBusinessError.BookDoesNotExist)
    }
  }
}
