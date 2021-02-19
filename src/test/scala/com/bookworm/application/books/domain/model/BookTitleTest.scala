package com.bookworm.application.books.domain.model

import com.bookworm.application.AbstractUnitTest

class BookTitleTest extends AbstractUnitTest {

  "BookTitle" should {
    "create a new instance given valid data" in {
      val bookTitleEither = BookTitle.create("Title")

      bookTitleEither.isRight shouldBe true
    }

    "return ValidationError#EmptyBookTitle if the title provided is empty" in {
      val bookTitleEither = BookTitle.create("")
      bookTitleEither shouldBe Left(ValidationError.EmptyBookTitle)
    }
  }
}

