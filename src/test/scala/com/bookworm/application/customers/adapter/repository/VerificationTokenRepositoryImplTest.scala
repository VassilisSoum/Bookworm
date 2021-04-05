package com.bookworm.application.customers.adapter.repository

import com.bookworm.application.customers.adapter.repository.dao.CustomerDao
import com.bookworm.application.customers.domain.model.{CustomerVerificationToken, VerificationToken}
import com.bookworm.application.integration.customers.TestData

import java.time.LocalDateTime
import java.util.UUID

class VerificationTokenRepositoryImplTest extends TestData {

  val customerDao = injector.getInstance(classOf[CustomerDao])

  override def beforeAll(): Unit = {
    runInTransaction(customerDao.insert(testPendingCustomer))
    super.beforeAll()
  }

  "VerificationTokenRepository" should {
    fakeClock.current = LocalDateTime
      .of(2025, 2, 7, 10, 0, 0)
      .atZone(fakeClock.zoneId)
      .toInstant

    val verificationTokenRepository = injector.getInstance(classOf[VerificationTokenRepositoryImpl])

    "save a customer verification token" in {
      val customerVerificationToken = CustomerVerificationToken(
        VerificationToken(UUID.randomUUID()),
        testCustomerId,
        LocalDateTime.now(fakeClock).plusSeconds(10)
      )

      runInTransaction(verificationTokenRepository.save(customerVerificationToken))

      val retrievedCustomerVerificationToken =
        runInTransaction(verificationTokenRepository.findBy(customerVerificationToken.token))

      retrievedCustomerVerificationToken.isDefined shouldBe true
      retrievedCustomerVerificationToken.get.token shouldBe customerVerificationToken.token
      retrievedCustomerVerificationToken.get.customerId shouldBe customerVerificationToken.customerId
      retrievedCustomerVerificationToken.get.expirationDate shouldBe customerVerificationToken.expirationDate
    }

    "remove all verification tokens for a customer" in {
      val verificationToken1 = VerificationToken(UUID.randomUUID())
      val verificationToken2 = VerificationToken(UUID.randomUUID())
      val customerVerificationToken = CustomerVerificationToken(
        verificationToken1,
        testCustomerId,
        LocalDateTime.now(fakeClock).plusSeconds(10)
      )

      runInTransaction(
        verificationTokenRepository
          .save(customerVerificationToken)
          .flatMap(_ =>
            verificationTokenRepository.save(
              customerVerificationToken.copy(token = verificationToken2)
            )
          )
          .flatMap(_ => verificationTokenRepository.removeAll(testCustomerId))
      )

      runInTransaction(verificationTokenRepository.findBy(verificationToken1)).isEmpty shouldBe true
      runInTransaction(verificationTokenRepository.findBy(verificationToken2)).isEmpty shouldBe true
    }
  }

}
