package com.bookworm.application.integration.customers

import cats.free.Free
import com.bookworm.application.IntegrationTestModule
import com.bookworm.application.customers.domain.model._
import doobie.free.connection
import doobie.implicits._

import java.time.Instant
import java.util.UUID

trait TestData extends IntegrationTestModule {

  val testCustomerId = CustomerId(UUID.randomUUID())
  val testCustomerFirstName = CustomerFirstName.create("Bill").toOption.get
  val testCustomerLastName = CustomerLastName.create("Soumakis").toOption.get
  val testCustomerEmail = CustomerEmail.create("someone@test.com").toOption.get
  val testCustomerAge = CustomerAge.create(28).toOption.get
  val testCustomerPassword = CustomerPassword.create("Someone@123").toOption.get

  val testPendingCustomer = Customer(
    customerId = testCustomerId,
    customerDetails = CustomerDetails(
      customerFirstName = testCustomerFirstName,
      customerLastName = testCustomerLastName,
      customerEmail = testCustomerEmail,
      customerAge = testCustomerAge
    ),
    customerPassword = testCustomerPassword,
    customerRegistrationStatus = CustomerRegistrationStatus.Pending
  )

  override def afterAll(): Unit = {
    clear()
    super.afterAll()
  }

  def runInTransaction[A](transaction: Free[connection.ConnectionOp, A]): A =
    transaction.transact(this.synchronousTransactor).unsafeRunSync()

  def advanceClockInMillis(value: Long): Instant = {
    fakeClock.current = fakeClock.current.plusMillis(value)
    fakeClock.current
  }

  def setClockAt(value: Instant): Unit =
    fakeClock.current = value

  def clear(): Unit = {
    val transaction = for {
      _ <- sql"""truncate table bookworm.CUSTOMER CASCADE""".update.run
      _ <- sql"""truncate table bookworm.CUSTOMER_VERIFICATION_TOKEN CASCADE""".update.run
    } yield ()

    transaction.transact(this.synchronousTransactor).unsafeRunSync()
  }
}
