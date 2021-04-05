package com.bookworm.application.customers.domain.model

import com.bookworm.application.AbstractUnitTest

class CustomerPasswordTest extends AbstractUnitTest {

  "CustomerPassword" should {
    "return CustomerPassword given a valid password" in {
      val customerPassword = CustomerPassword.create("Someone@123")

      customerPassword.isRight shouldBe true
    }

    "return InvalidCustomerPassword if the password is less than 8 characters" in {
      val customerPassword = CustomerPassword.create("s@123")

      customerPassword.isLeft shouldBe true

      customerPassword.left.toOption.get shouldBe DomainValidationError.InvalidCustomerPassword
    }

    "return InvalidCustomerPassword if the password is less than 20 characters" in {
      val customerPassword = CustomerPassword.create("s@12345678902028928928478776")

      customerPassword.isLeft shouldBe true

      customerPassword.left.toOption.get shouldBe DomainValidationError.InvalidCustomerPassword
    }

    "return InvalidCustomerPassword if the password does not meet the requirements" in {
      val customerPassword = CustomerPassword.create("s@12345678908")

      customerPassword.isLeft shouldBe true

      customerPassword.left.toOption.get shouldBe DomainValidationError.InvalidCustomerPassword
    }
  }

}
