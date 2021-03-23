package com.bookworm.application.customers.domain.model

import com.bookworm.application.AbstractUnitTest

class CustomerLastNameTest extends AbstractUnitTest {

  "CustomerLastName" should {
    "return CustomerLastName given valid data" in {
      val customerLastName = CustomerLastName.create("Bill")

      customerLastName.isRight shouldBe true
    }

    "return InvalidCustomerLastName if the provided last name is empty" in {
      val customerLastName = CustomerLastName.create("")

      customerLastName.left.toOption.get shouldBe DomainValidationError.InvalidCustomerLastName
    }
  }

}
