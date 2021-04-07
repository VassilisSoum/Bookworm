package com.bookworm.application.integration.customers

import cats.effect.IO
import com.bookworm.application.config.Configuration.CustomerConfig
import com.bookworm.application.customers.adapter.api.dto.{BusinessErrorDto, CompleteCustomerRegistrationRequestDto, CustomerRegistrationRequestDto, ValidationErrorDto}
import com.bookworm.application.customers.adapter.repository.dao.{CustomerDao, CustomerVerificationTokenDao}
import com.bookworm.application.customers.domain.model.{CustomerId, CustomerRegistrationStatus, DomainBusinessError, DomainValidationError}
import org.http4s._
import org.scalatest.MustMatchers.convertToAnyMustWrapper

import java.time.LocalDateTime
import java.util.UUID

class CustomerRegistrationIntegrationTest
  extends TestData
  with CustomerEndpoints
  with EntityEncoders
  with EntityDecoders {

  override def beforeEach(): Unit = {
    clear()
    super.beforeEach()
  }

  "Initiating a customer registration" should {
    fakeClock.current = LocalDateTime
      .of(2025, 2, 7, 10, 0, 0)
      .atZone(fakeClock.zoneId)
      .toInstant

    val customerRegistrationRequestDto = CustomerRegistrationRequestDto(
      testCustomerFirstName.value,
      testCustomerLastName.value,
      testCustomerEmail.value,
      testCustomerAge.value,
      testCustomerPassword.value
    )

    val customerDao: CustomerDao = injector.getInstance(classOf[CustomerDao])
    val customerVerificationTokenDao: CustomerVerificationTokenDao =
      injector.getInstance(classOf[CustomerVerificationTokenDao])

    val customerApplicationConfig = injector.getInstance(classOf[CustomerConfig])

    "return 204 NO CONTENT and add the customer with registration status PENDING " +
    "and save the email verification token" in {
      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto)
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.NoContent

      val customerQueryModelOpt = runInTransaction(customerDao.getOptionalByCustomerEmail(testCustomerEmail))

      customerQueryModelOpt.isDefined mustBe true
      customerQueryModelOpt.head.customerFirstName mustBe testPendingCustomer.customerDetails.customerFirstName.value
      customerQueryModelOpt.head.customerLastName mustBe testPendingCustomer.customerDetails.customerLastName.value
      customerQueryModelOpt.head.customerEmail mustBe testPendingCustomer.customerDetails.customerEmail.value
      customerQueryModelOpt.head.customerAge mustBe testPendingCustomer.customerDetails.customerAge.value
      customerQueryModelOpt.head.customerRegistrationStatus mustBe testPendingCustomer.customerRegistrationStatus

      val customerVerificationTokens = runInTransaction(
        customerVerificationTokenDao.getAllCustomerVerificationTokensByCustomerId(
          CustomerId(customerQueryModelOpt.head.customerId)
        )
      )

      customerVerificationTokens.size mustBe 1
      customerVerificationTokens.head.customerId.id mustBe customerQueryModelOpt.head.customerId
      customerVerificationTokens.head.expirationDate mustBe LocalDateTime
        .now(fakeClock)
        .plusSeconds(customerApplicationConfig.verificationTokenExpirationInSeconds)
    }

    "return 204 NO CONTENT and complete a customer registration" in {
      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto)
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.NoContent

      val customerQueryModelOpt = runInTransaction(customerDao.getOptionalByCustomerEmail(testCustomerEmail))

      val customerVerificationToken = runInTransaction(
        customerVerificationTokenDao.getAllCustomerVerificationTokensByCustomerId(
          CustomerId(customerQueryModelOpt.head.customerId)
        )
      ).head

      val completeCustomerRegistrationRequestDto =
        CompleteCustomerRegistrationRequestDto(customerVerificationToken.token.value)
      val completeCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/complete-registration"))
        .withEntity(completeCustomerRegistrationRequestDto)

      val completeCustomerResponse = endpoint(completeCustomerRequest).unsafeRunSync()
      completeCustomerResponse.status mustBe Status.NoContent

      //Since the customer has completed registration then the status is changed to Completed
      runInTransaction(
        customerDao.getOptionalByCustomerEmail(testCustomerEmail)
      ).head.customerRegistrationStatus mustBe CustomerRegistrationStatus.Completed
    }

    "return 409 CONFLICT with error type VerificationTokenDoesNotExists when trying to complete a customer registration" in {
      val unknownVerificationToken = UUID.randomUUID()
      val completeCustomerRegistrationRequestDto =
        CompleteCustomerRegistrationRequestDto(unknownVerificationToken)
      val completeCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/complete-registration"))
        .withEntity(completeCustomerRegistrationRequestDto)

      val completeCustomerResponse = endpoint(completeCustomerRequest).unsafeRunSync()
      completeCustomerResponse.status mustBe Status.Conflict

      completeCustomerResponse
        .as[BusinessErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainBusinessError.VerificationTokenDoesNotExists
    }

    "return 409 CONFLICT with error type VerificationTokenExpired when trying to complete a customer registration" in {
      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto)
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.NoContent

      val customerQueryModelOpt = runInTransaction(customerDao.getOptionalByCustomerEmail(testCustomerEmail))

      val customerVerificationToken = runInTransaction(
        customerVerificationTokenDao.getAllCustomerVerificationTokensByCustomerId(
          CustomerId(customerQueryModelOpt.head.customerId)
        )
      ).head

      //Forward current time so as the customer verification token to be expired
      fakeClock.current = LocalDateTime
        .now(fakeClock)
        .plusSeconds(customerApplicationConfig.verificationTokenExpirationInSeconds + 1)
        .atZone(fakeClock.zoneId)
        .toInstant

      val completeCustomerRegistrationRequestDto =
        CompleteCustomerRegistrationRequestDto(customerVerificationToken.token.value)
      val completeCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/complete-registration"))
        .withEntity(completeCustomerRegistrationRequestDto)

      val completeCustomerResponse = endpoint(completeCustomerRequest).unsafeRunSync()
      completeCustomerResponse.status mustBe Status.Conflict

      completeCustomerResponse
        .as[BusinessErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainBusinessError.VerificationTokenExpired

      //Since the customer has not completed registration then the status is kept to Pending
      runInTransaction(
        customerDao.getOptionalByCustomerEmail(testCustomerEmail)
      ).head.customerRegistrationStatus mustBe CustomerRegistrationStatus.Pending
    }

    //TODO: Generate random data

    "return 409 CONFLICT with error type CustomerAlreadyExists when initiating a customer registration" in {
      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto)
      var initiateCustomerResponse: Response[IO] = endpoint(initiateCustomerRequest).unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.NoContent

      initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.Conflict
      initiateCustomerResponse
        .as[BusinessErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainBusinessError.CustomerAlreadyExists
    }

    "return 400 BAD REQUEST with error type InvalidCustomerFirstName" in {
      fakeClock.current = LocalDateTime
        .of(2025, 2, 7, 10, 0, 0)
        .atZone(fakeClock.zoneId)
        .toInstant

      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto.copy(firstName = ""))
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.BadRequest
      initiateCustomerResponse
        .as[ValidationErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainValidationError.InvalidCustomerFirstName
    }

    "return 400 BAD REQUEST with error type InvalidCustomerLastName" in {
      fakeClock.current = LocalDateTime
        .of(2025, 2, 7, 10, 0, 0)
        .atZone(fakeClock.zoneId)
        .toInstant

      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto.copy(lastName = ""))
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.BadRequest
      initiateCustomerResponse
        .as[ValidationErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainValidationError.InvalidCustomerLastName
    }

    "return 400 BAD REQUEST with error type InvalidCustomerEmail" in {
      fakeClock.current = LocalDateTime
        .of(2025, 2, 7, 10, 0, 0)
        .atZone(fakeClock.zoneId)
        .toInstant

      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto.copy(email = "s@+/.com"))
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.BadRequest
      initiateCustomerResponse
        .as[ValidationErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainValidationError.InvalidCustomerEmail
    }

    "return 400 BAD REQUEST with error type InvalidCustomerAge for underage customers" in {
      fakeClock.current = LocalDateTime
        .of(2025, 2, 7, 10, 0, 0)
        .atZone(fakeClock.zoneId)
        .toInstant

      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto.copy(age = 17))
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.BadRequest
      initiateCustomerResponse
        .as[ValidationErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainValidationError.InvalidCustomerAge
    }

    "return 400 BAD REQUEST with error type InvalidCustomerPassword for password length for under 8 characters" in {
      fakeClock.current = LocalDateTime
        .of(2025, 2, 7, 10, 0, 0)
        .atZone(fakeClock.zoneId)
        .toInstant

      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto.copy(password = "123"))
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.BadRequest
      initiateCustomerResponse
        .as[ValidationErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainValidationError.InvalidCustomerPassword
    }

    "return 400 BAD REQUEST with error type InvalidCustomerPassword for password length for over 20 characters" in {
      fakeClock.current = LocalDateTime
        .of(2025, 2, 7, 10, 0, 0)
        .atZone(fakeClock.zoneId)
        .toInstant

      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto.copy(password = "1232381923712893719832"))
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.BadRequest
      initiateCustomerResponse
        .as[ValidationErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainValidationError.InvalidCustomerPassword
    }

    "return 400 BAD REQUEST with error type InvalidCustomerPassword for password length for not meeting requirements" in {
      fakeClock.current = LocalDateTime
        .of(2025, 2, 7, 10, 0, 0)
        .atZone(fakeClock.zoneId)
        .toInstant

      val initiateCustomerRequest = Request[IO](Method.POST, Uri.unsafeFromString(s"/customers/registration"))
        .withEntity(customerRegistrationRequestDto.copy(password = "someone@123"))
      val initiateCustomerResponse = endpoint(initiateCustomerRequest)
        .unsafeRunSync()
      initiateCustomerResponse.status mustBe Status.BadRequest
      initiateCustomerResponse
        .as[ValidationErrorDto]
        .unsafeRunSync()
        .errorType mustBe DomainValidationError.InvalidCustomerPassword
    }
  }

}
