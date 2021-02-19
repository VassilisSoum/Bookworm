package com.bookworm.application.books.domain.model

import com.bookworm.application.AbstractUnitTest

class GenreNameTest extends AbstractUnitTest {

  "GenreName" should {
    "create a new instance given valid data" in {
      val genreNameEither = GenreName.create("Genre")

      genreNameEither.isRight shouldBe true
    }

    "return ValidationError#EmptyGenreName if the genre name provided is empty" in {
      val genreNameEither = GenreName.create("")
      genreNameEither shouldBe Left(ValidationError.EmptyGenreName)
    }
  }
}