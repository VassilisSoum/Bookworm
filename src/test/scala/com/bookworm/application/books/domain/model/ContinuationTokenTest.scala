package com.bookworm.application.books.domain.model

import com.bookworm.application.AbstractUnitTest

class ContinuationTokenTest extends AbstractUnitTest {

  "ContinuationToken" should {
    "create a new instance given valid data" in {
      val token = "29-01-2021T21:00:45_18727"

      ContinuationToken.create(token).isRight shouldBe true
    }

    "return ValidationError#InvalidContinuationTokenFormat if the provided token does not contain an underscore separator" in {
      val invalidToken = "29-01-2021T21:00:452828217"

      ContinuationToken.create(invalidToken).left.toOption.get shouldBe DomainValidationError.InvalidContinuationTokenFormat
    }

    "return ValidationError#EmptyContinuationToken if the provided token is empty" in {
      ContinuationToken.create("").left.toOption.get shouldBe DomainValidationError.EmptyContinuationToken
    }
  }

}
