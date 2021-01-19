package com.bookworm.application.books.domain.model

import org.scalatest.{Matchers, WordSpec}

class AuthorFirstNameTest extends WordSpec with Matchers {

  "AuthorFirstName" should {
    "create a new instance given valid data" in {
      val authorFirstNameEither = AuthorFirstName.create("FirstName")

      authorFirstNameEither.isRight shouldBe true
    }

    "return ValidationError#EmptyAuthorFirstName if the first name provided is empty" in {
      val authorFirstNameEither = AuthorFirstName.create("")
      authorFirstNameEither shouldBe Left(ValidationError.EmptyAuthorFirstName)
    }
  }
}
