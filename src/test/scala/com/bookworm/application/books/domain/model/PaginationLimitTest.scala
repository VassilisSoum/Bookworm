package com.bookworm.application.books.domain.model

import org.scalatest.{Matchers, WordSpec}

class PaginationLimitTest extends WordSpec with Matchers {

  "PaginationLimit" should {
    "create a new instance given valid data" in {
      val limit = 10

      PaginationLimit.create(limit).isRight shouldBe true
    }

    "return ValidationError#NonPositivePaginationLimit when the provided limit is non-positive" in {
      val limit = -1

      PaginationLimit.create(limit).left.toOption.get shouldBe ValidationError.NonPositivePaginationLimit
    }

    "return ValidationError#PaginationLimitExceedsMaximum when the provided limit exceeds maximum" in {
      val limit = 1000

      PaginationLimit.create(limit).left.toOption.get shouldBe ValidationError.PaginationLimitExceedsMaximum
    }
  }
}
