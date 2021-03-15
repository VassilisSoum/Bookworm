package com.bookworm.application.books.domain.port.inbound

import cats.Id
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.books.domain.model.DomainBusinessError
import com.bookworm.application.books.domain.port.inbound.query.AuthorsByBookIdQuery
import com.bookworm.application.books.domain.port.outbound.{AuthorRepository, BookRepository}

class GetAuthorsByBookIdUseCaseTest extends AbstractUnitTest {

  val bookRepository: BookRepository[Id] = mock[BookRepository[Id]]
  val authorRepository: AuthorRepository[Id] = mock[AuthorRepository[Id]]
  val getAuthorsByBookIdUseCase = new GetAuthorsByBookIdUseCase[Id](bookRepository, authorRepository)

  "GetAuthorsByBookIdUseCase" should {
    "retrieve all the authors given a book id of a book that exists" in {
      (bookRepository.getById _).expects(testBook.bookId).returns(Some(testBookQueryModel)).once()
      (authorRepository.getAllByBookId _).expects(testBook.bookId).returns(List(testAuthorQueryModel)).once()

      getAuthorsByBookIdUseCase.retrieveAuthorsByBookId(testBook.bookId).toOption.get shouldBe AuthorsByBookIdQuery(
        List(testAuthorQueryModel)
      )
    }

    "return BookDoesNotExist" in {
      (bookRepository.getById _).expects(testBook.bookId).returns(None).once()
      (authorRepository.getAllByBookId _).expects(*).never()

      getAuthorsByBookIdUseCase
        .retrieveAuthorsByBookId(testBook.bookId)
        .left
        .toOption
        .get shouldBe DomainBusinessError.BookDoesNotExist
    }
  }
}
