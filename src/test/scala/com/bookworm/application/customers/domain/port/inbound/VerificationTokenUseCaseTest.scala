package com.bookworm.application.customers.domain.port.inbound

import cats.Id
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.customers.domain.model.{CustomerId, DomainBusinessError, VerificationToken}
import com.bookworm.application.customers.domain.port.inbound.command.SaveEmailVerificationTokenCommand
import com.bookworm.application.customers.domain.port.outbound.{CustomerRepository, VerificationTokenRepository}

import java.time.LocalDateTime
import java.util.UUID

class VerificationTokenUseCaseTest extends AbstractUnitTest {

  val customerRepository = mock[CustomerRepository[Id]]
  val verificationTokenRepository = mock[VerificationTokenRepository[Id]]
  val verificationTokenUseCase = new VerificationTokenUseCase[Id](customerRepository, verificationTokenRepository)

  "VerificationTokenUseCase" should {
    val saveEmailVerificationTokenCommand = SaveEmailVerificationTokenCommand(
      token = VerificationToken(UUID.randomUUID()),
      customerId = customerId,
      expirationDate = LocalDateTime.of(2025, 12, 12, 10, 0, 0)
    )
    "removes previous customer verification tokens and " +
    "save the verification token for an existing customer but not yet fully registered" in {
      (customerRepository.findBy(_: CustomerId)).expects(customerId).returns(Some(pendingCustomerQueryModel)).once()
      (verificationTokenRepository.removeAll _).expects(customerId).returns(()).once()
      (verificationTokenRepository.save _).expects(saveEmailVerificationTokenCommand.toDomainObject).returns(()).once()

      verificationTokenUseCase.saveEmailVerificationToken(saveEmailVerificationTokenCommand).isRight shouldBe true
    }

    "returns CustomerDoesNotExists when trying to save a verification token for non existent customer" in {
      (customerRepository.findBy(_: CustomerId)).expects(customerId).returns(None).once()
      (verificationTokenRepository.removeAll _).expects(*).never()
      (verificationTokenRepository.save _).expects(*).never()

      verificationTokenUseCase
        .saveEmailVerificationToken(saveEmailVerificationTokenCommand)
        .left
        .toOption
        .get shouldBe DomainBusinessError.CustomerDoesNotExists
    }

    "returns CustomerDoesNotExists when trying to save a verification token for a customer " +
    "whom the registration status is expired" in {
      (customerRepository.findBy(_: CustomerId)).expects(customerId).returns(Some(expiredRegistrationCustomerQueryModel)).once()
      (verificationTokenRepository.removeAll _).expects(*).never()
      (verificationTokenRepository.save _).expects(*).never()

      verificationTokenUseCase
        .saveEmailVerificationToken(saveEmailVerificationTokenCommand)
        .left
        .toOption
        .get shouldBe DomainBusinessError.CustomerDoesNotExists
    }

    "returns CustomerAlreadyRegistered when trying to save a verification token for a fully registered customer" in {
      (customerRepository.findBy(_: CustomerId)).expects(customerId).returns(Some(registeredCustomerQueryModel)).once()
      (verificationTokenRepository.removeAll _).expects(*).never()
      (verificationTokenRepository.save _).expects(*).never()

      verificationTokenUseCase
        .saveEmailVerificationToken(saveEmailVerificationTokenCommand)
        .left
        .toOption
        .get shouldBe DomainBusinessError.CustomerAlreadyRegistered
    }
  }

}
