package com.bookworm.application.books.domain.model

import com.bookworm.application.UnitSpec

class AuthorLastNameTest extends UnitSpec {

  "AuthorLastName" should {
    "create a new instance given valid data" in {
      val authorLastNameEither = AuthorLastName.create("LastName")

      authorLastNameEither.isRight shouldBe true
    }

    "return ValidationError#EmptyAuthorLastName if the last name provided is empty" in {
      val authorLastNameEither = AuthorLastName.create("")
      authorLastNameEither shouldBe Left(ValidationError.EmptyAuthorLastName)
    }
  }
}
