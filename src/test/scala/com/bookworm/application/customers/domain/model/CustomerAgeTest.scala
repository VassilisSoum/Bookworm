package com.bookworm.application.customers.domain.model

import com.bookworm.application.AbstractUnitTest

class CustomerAgeTest extends AbstractUnitTest {

  "CustomerAge" should {
    "return CustomerAge given valid data" in {
      val customerAge = CustomerAge.create(28)

      customerAge.isRight shouldBe true
    }

    "return InvalidCustomerAge if the provided age is below minimum age" in {
      val customerAge = CustomerAge.create(17)

      customerAge.left.toOption.get shouldBe DomainValidationError.InvalidCustomerAge
    }
  }

}
