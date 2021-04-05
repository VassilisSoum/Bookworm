package com.bookworm.application.customers.adapter.repository

import com.bookworm.application.customers.adapter.repository.dao.CustomerDao
import com.bookworm.application.customers.domain.model.{Customer, CustomerDetails, CustomerEmail, CustomerId, CustomerRegistrationStatus}
import com.bookworm.application.integration.customers.TestData

import java.time.LocalDateTime
import java.util.UUID

class CustomerRepositoryImplTest extends TestData {

  val customerDao = injector.getInstance(classOf[CustomerDao])

  override def beforeAll(): Unit = {
    runInTransaction(customerDao.insert(testPendingCustomer))
    super.beforeAll()
  }

  "CustomerRepositoryImpl" should {
    fakeClock.current = LocalDateTime
      .of(2025, 2, 7, 10, 0, 0)
      .atZone(fakeClock.zoneId)
      .toInstant

    val customerRepository = injector.getInstance(classOf[CustomerRepositoryImpl])

    "return true when an email exists for a customer" in {
      runInTransaction(customerRepository.exists(testCustomerEmail)) shouldBe true
    }

    "return false when an email does not exists already" in {
      runInTransaction(
        customerRepository.exists(CustomerEmail.create("something-unknown@test.com").toOption.get)
      ) shouldBe false
    }

    "return the customer given its id" in {
      val customerQueryModel = runInTransaction(customerRepository.findBy(testCustomerId))

      customerQueryModel.isDefined shouldBe true
      customerQueryModel.head.customerId shouldBe testCustomerId.id
      customerQueryModel.head.customerFirstName shouldBe testCustomerFirstName.value
      customerQueryModel.head.customerLastName shouldBe testCustomerLastName.value
      customerQueryModel.head.customerAge shouldBe testCustomerAge.value
      customerQueryModel.head.customerEmail shouldBe testCustomerEmail.value
      customerQueryModel.head.customerRegistrationStatus shouldBe CustomerRegistrationStatus.Pending
    }

    "save a new customer" in {
      val newCustomer: Customer = createCustomer(CustomerEmail.create("anewemail@test.com").toOption.get)

      val retrievedNewCustomer = runInTransaction(
        customerRepository.save(newCustomer).flatMap(_ => customerRepository.findBy(newCustomer.customerId))
      )

      retrievedNewCustomer.isDefined shouldBe true
      retrievedNewCustomer.head.customerId shouldBe newCustomer.customerId.id
      retrievedNewCustomer.head.customerFirstName shouldBe newCustomer.customerDetails.customerFirstName.value
      retrievedNewCustomer.head.customerLastName shouldBe newCustomer.customerDetails.customerLastName.value
      retrievedNewCustomer.head.customerEmail shouldBe newCustomer.customerDetails.customerEmail.value
      retrievedNewCustomer.head.customerAge shouldBe newCustomer.customerDetails.customerAge.value
      retrievedNewCustomer.head.customerRegistrationStatus shouldBe newCustomer.customerRegistrationStatus
    }

    "update a customer's registration status" in {
      val newCustomer: Customer = createCustomer(CustomerEmail.create("anewemail2@test.com").toOption.get)

      val customerQueryModel = runInTransaction(
        customerRepository
          .save(newCustomer)
          .flatMap(_ =>
            customerRepository.updateRegistrationStatus(newCustomer.customerId, CustomerRegistrationStatus.Completed)
          ).flatMap(_ => customerRepository.findBy(newCustomer.customerId))
      )

      customerQueryModel.isDefined shouldBe true
      customerQueryModel.head.customerRegistrationStatus shouldBe CustomerRegistrationStatus.Completed
    }
  }

  private def createCustomer(customerEmail: CustomerEmail) = {
    val newCustomer = Customer(
      customerId = CustomerId(UUID.randomUUID()),
      customerDetails = CustomerDetails(
        customerFirstName = testCustomerFirstName,
        customerLastName = testCustomerLastName,
        customerEmail = customerEmail,
        customerAge = testCustomerAge
      ),
      customerPassword = testCustomerPassword,
      customerRegistrationStatus = CustomerRegistrationStatus.Pending
    )
    newCustomer
  }
}
