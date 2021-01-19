package com.bookworm.application.books.domain.model

import org.scalatest.{Matchers, WordSpec}

class GenreNameTest extends WordSpec with Matchers {

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