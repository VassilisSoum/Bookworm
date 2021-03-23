package com.bookworm.application.customers.domain.model

import com.bookworm.application.AbstractUnitTest

class CustomerEmailTest extends AbstractUnitTest {

  "CustomerEmail" should {
    "return CustomerEmail given valid email" in {
      val customerEmail = CustomerEmail.create("someone@test.com")

      customerEmail.isRight shouldBe true
    }

    "return InvalidCustomerEmail if the provided email is invalid email" in {
      val customerEmail = CustomerEmail.create("invalid.@test.com")

      customerEmail.left.toOption.get shouldBe DomainValidationError.InvalidCustomerEmail
    }
  }

}
