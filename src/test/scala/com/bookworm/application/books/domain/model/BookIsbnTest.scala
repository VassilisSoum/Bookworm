package com.bookworm.application.books.domain.model

import com.bookworm.application.UnitSpec

class BookIsbnTest extends UnitSpec {

  "BookIsbn" should {
    "create a new instance given valid data" in {
      val bookIsbnEither = BookIsbn.create("Isbn")

      bookIsbnEither.isRight shouldBe true
    }

    "return ValidationError#EmptyBookIsbn if the isbn provided is empty" in {
      val bookIsbnEither = BookIsbn.create("")
      bookIsbnEither shouldBe Left(ValidationError.EmptyBookIsbn)
    }
  }
}
