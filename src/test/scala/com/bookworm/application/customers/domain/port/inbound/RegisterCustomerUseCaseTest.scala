package com.bookworm.application.customers.domain.port.inbound

import cats.Id
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.customers.domain.model.{DomainBusinessError, VerificationToken}
import com.bookworm.application.customers.domain.port.inbound.command.{CompleteCustomerRegistrationCommand, InitiateCustomerRegistrationCommand}
import com.bookworm.application.customers.domain.port.outbound.{CustomerRepository, VerificationTokenRepository}

import java.time.LocalDateTime
import java.util.UUID

class RegisterCustomerUseCaseTest extends AbstractUnitTest {

  val customerRepository = mock[CustomerRepository[Id]]
  val verificationTokenRepository = mock[VerificationTokenRepository[Id]]
  val registerCustomerUseCase = new RegisterCustomerUseCase(customerRepository, verificationTokenRepository, fixedClock)

  "RegisterCustomerUseCase" should {
    val initiateCustomerRegistrationCommand =
      InitiateCustomerRegistrationCommand(customerId, customerFirstName, customerLastName, customerEmail, customerAge)

    val completeCustomerRegistrationCommand = CompleteCustomerRegistrationCommand(VerificationToken(UUID.randomUUID()))
    "save a new customer in a pending state" in {
      (customerRepository.exists _).expects(customerEmail).returns(false).once()
      (customerRepository.save _).expects(initiateCustomerRegistrationCommand.toDomainObject).returns(()).once()

      registerCustomerUseCase.initiateRegistration(initiateCustomerRegistrationCommand).isRight shouldBe true
    }

    "return CustomerAlreadyExists when trying to register a customer with the same email" in {
      (customerRepository.exists _).expects(customerEmail).returns(true).once()
      (customerRepository.save _).expects(*).never()

      registerCustomerUseCase
        .initiateRegistration(initiateCustomerRegistrationCommand)
        .left
        .toOption
        .get shouldBe DomainBusinessError.CustomerAlreadyExists
    }

    "complete a customer registration for a customer with pending registration" in {
      (verificationTokenRepository.findBy _)
        .expects(completeCustomerRegistrationCommand.verificationToken)
        .returns(Some(customerVerificationToken))
        .once()

      (customerRepository.findBy _).expects(customerId).returns(Some(pendingCustomer)).once()
      (customerRepository.update _).expects(registeredCustomer).returns(()).once()

      registerCustomerUseCase.completeCustomerRegistration(completeCustomerRegistrationCommand).isRight shouldBe true
    }

    "return VerificationTokenExpired when trying to " +
    "complete a registration for a pending customer and the verification token has expired" in {
      (verificationTokenRepository.findBy _)
        .expects(completeCustomerRegistrationCommand.verificationToken)
        .returns(Some(customerVerificationToken.copy(expirationDate = LocalDateTime.now(fixedClock).minusMinutes(1))))
        .once()

      (customerRepository.findBy _).expects(*).never()
      (customerRepository.update _).expects(*).never()

      registerCustomerUseCase
        .completeCustomerRegistration(completeCustomerRegistrationCommand)
        .left
        .toOption
        .get shouldBe DomainBusinessError.VerificationTokenExpired
    }

    "return VerificationTokenDoesNotExists when trying to " +
    "complete a registration for a pending customer and the verification token does not exist" in {
      (verificationTokenRepository.findBy _)
        .expects(completeCustomerRegistrationCommand.verificationToken)
        .returns(None)
        .once()

      (customerRepository.findBy _).expects(*).never()
      (customerRepository.update _).expects(*).never()

      registerCustomerUseCase
        .completeCustomerRegistration(completeCustomerRegistrationCommand)
        .left
        .toOption
        .get shouldBe DomainBusinessError.VerificationTokenDoesNotExists
    }

    "return CustomerDoesNotExists when trying to " +
    "complete a registration for a customer than does not exist" in {
      (verificationTokenRepository.findBy _)
        .expects(completeCustomerRegistrationCommand.verificationToken)
        .returns(Some(customerVerificationToken))
        .once()

      (customerRepository.findBy _).expects(customerId).returns(None).once()
      (customerRepository.update _).expects(*).never()

      registerCustomerUseCase
        .completeCustomerRegistration(completeCustomerRegistrationCommand)
        .left
        .toOption
        .get shouldBe DomainBusinessError.CustomerDoesNotExists
    }
  }
}
