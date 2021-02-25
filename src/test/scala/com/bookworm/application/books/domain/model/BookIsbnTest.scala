package com.bookworm.application.books.domain.model

import com.bookworm.application.AbstractUnitTest

class BookIsbnTest extends AbstractUnitTest {

  "BookIsbn" should {
    "create a new instance given valid data" in {
      val bookIsbnEither = BookIsbn.create("9781234567897")

      bookIsbnEither.isRight shouldBe true
    }

    "return ValidationError#EmptyBookIsbn if the isbn provided is empty" in {
      val bookIsbnEither = BookIsbn.create("")
      bookIsbnEither shouldBe Left(DomainValidationError.EmptyBookIsbn)
    }

    "return ValidationError#InvalidIsbnLength if the isbn provided is not 13 characters length" in {
      val bookIsbnEither = BookIsbn.create("1234")
      bookIsbnEither shouldBe Left(DomainValidationError.InvalidIsbnLength)
    }
  }
}
