package com.bookworm.application.customers.domain.model

import com.bookworm.application.AbstractUnitTest

class CustomerFirstNameTest extends AbstractUnitTest {

  "CustomerFirstName" should {
    "return CustomerFirstName given valid data" in {
      val customerFirstName = CustomerFirstName.create("Bill")

      customerFirstName.isRight shouldBe true
    }

    "return InvalidCustomerFirstName if the provided first name is empty" in {
      val customerFirstName = CustomerFirstName.create("")

      customerFirstName.left.toOption.get shouldBe DomainValidationError.InvalidCustomerFirstName
    }
  }

}
