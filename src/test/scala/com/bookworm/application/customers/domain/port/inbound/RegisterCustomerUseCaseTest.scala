package com.bookworm.application.customers.domain.port.inbound

import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.customers.domain.model.{CustomerRegistrationStatus, DomainBusinessError, VerificationToken}
import com.bookworm.application.customers.domain.port.inbound.command.{CompleteCustomerRegistrationCommand, InitiateCustomerRegistrationCommand}
import com.bookworm.application.customers.domain.port.outbound.{CustomerRepository, VerificationTokenRepository}

import java.time.LocalDateTime
import java.util.UUID
import scala.util.{Success, Try}

class RegisterCustomerUseCaseTest extends AbstractUnitTest {

  val customerRepository = mock[CustomerRepository[Try]]
  val verificationTokenRepository = mock[VerificationTokenRepository[Try]]

  val registerCustomerUseCase =
    new RegisterCustomerUseCase[Try](customerRepository, verificationTokenRepository, fixedClock)

  "RegisterCustomerUseCase" should {
    val initiateCustomerRegistrationCommand =
      InitiateCustomerRegistrationCommand(
        customerId,
        customerFirstName,
        customerLastName,
        customerEmail,
        customerAge,
        customerPassword
      )

    val completeCustomerRegistrationCommand = CompleteCustomerRegistrationCommand(VerificationToken(UUID.randomUUID()))
    "save a new customer in a pending state" in {
      (customerRepository.exists _).expects(customerEmail).returns(Success(false)).once()
      (customerRepository.save _)
        .expects(initiateCustomerRegistrationCommand.toDomainObject)
        .returns(Success(()))
        .once()

      registerCustomerUseCase.initiateRegistration(initiateCustomerRegistrationCommand).get.isRight shouldBe true
    }

    "return CustomerAlreadyExists when trying to register a customer with the same email" in {
      (customerRepository.exists _).expects(customerEmail).returns(Success(true)).once()
      (customerRepository.save _).expects(*).never()

      registerCustomerUseCase
        .initiateRegistration(initiateCustomerRegistrationCommand)
        .get
        .left
        .toOption
        .get shouldBe DomainBusinessError.CustomerAlreadyExists
    }

    "complete a customer registration for a customer with pending registration" in {
      (verificationTokenRepository.findBy _)
        .expects(completeCustomerRegistrationCommand.verificationToken)
        .returns(Success(Some(customerVerificationToken)))
        .once()

      (customerRepository.findBy _).expects(customerId).returns(Success(Some(pendingCustomerQueryModel))).once()
      (customerRepository.updateRegistrationStatus _)
        .expects(customerId, CustomerRegistrationStatus.Completed)
        .returns(Success(()))
        .once()

      registerCustomerUseCase
        .completeCustomerRegistration(completeCustomerRegistrationCommand)
        .get
        .isRight shouldBe true
    }

    "return VerificationTokenExpired when trying to " +
    "complete a registration for a pending customer and the verification token has expired" in {
      (verificationTokenRepository.findBy _)
        .expects(completeCustomerRegistrationCommand.verificationToken)
        .returns(
          Success(Some(customerVerificationToken.copy(expirationDate = LocalDateTime.now(fixedClock).minusMinutes(1))))
        )
        .once()

      (customerRepository.findBy _).expects(*).never()
      (customerRepository.updateRegistrationStatus _).expects(*, *).never()

      registerCustomerUseCase
        .completeCustomerRegistration(completeCustomerRegistrationCommand)
        .get
        .left
        .toOption
        .get shouldBe DomainBusinessError.VerificationTokenExpired
    }

    "return VerificationTokenDoesNotExists when trying to " +
    "complete a registration for a pending customer and the verification token does not exist" in {
      (verificationTokenRepository.findBy _)
        .expects(completeCustomerRegistrationCommand.verificationToken)
        .returns(Success(None))
        .once()

      (customerRepository.findBy _).expects(*).never()
      (customerRepository.updateRegistrationStatus _).expects(*, *).never()

      registerCustomerUseCase
        .completeCustomerRegistration(completeCustomerRegistrationCommand)
        .get
        .left
        .toOption
        .get shouldBe DomainBusinessError.VerificationTokenDoesNotExists
    }

    "return CustomerDoesNotExists when trying to " +
    "complete a registration for a customer than does not exist" in {
      (verificationTokenRepository.findBy _)
        .expects(completeCustomerRegistrationCommand.verificationToken)
        .returns(Success(Some(customerVerificationToken)))
        .once()

      (customerRepository.findBy _).expects(customerId).returns(Success(None)).once()
      (customerRepository.updateRegistrationStatus _).expects(*, *).never()

      registerCustomerUseCase
        .completeCustomerRegistration(completeCustomerRegistrationCommand)
        .get
        .left
        .toOption
        .get shouldBe DomainBusinessError.CustomerDoesNotExists
    }
  }
}
