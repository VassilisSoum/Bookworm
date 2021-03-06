package com.bookworm.application.books.domain.model

import com.bookworm.application.AbstractUnitTest

class BookSummaryTest extends AbstractUnitTest {

  "BookSummary" should {
    "create a new instance given valid data" in {
      val bookSummaryEither = BookSummary.create("Summary")

      bookSummaryEither.isRight shouldBe true
    }

    "return ValidationError#EmptyBookSummary if the summary provided is empty" in {
      val bookSummaryEither = BookSummary.create("")
      bookSummaryEither shouldBe Left(DomainValidationError.EmptyBookSummary)
    }
  }
}
