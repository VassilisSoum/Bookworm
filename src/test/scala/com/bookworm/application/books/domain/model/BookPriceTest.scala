package com.bookworm.application.books.domain.model

import com.bookworm.application.AbstractUnitTest

class BookPriceTest extends AbstractUnitTest {

  "BookPrice" should {
    "create a new instance given valid data" in {
      val bookPriceEither = BookPrice.create(1000L)

      bookPriceEither.isRight shouldBe true
    }

    "return ValidationError#NegativeBookPrice if the title provided is empty" in {
      val bookPriceEither = BookPrice.create(-500L)
      bookPriceEither shouldBe Left(DomainValidationError.NegativeBookPrice)
    }
  }

}
