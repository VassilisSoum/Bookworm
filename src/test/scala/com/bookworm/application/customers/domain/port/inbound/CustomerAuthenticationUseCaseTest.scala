package com.bookworm.application.customers.domain.port.inbound

import cats.Id
import com.bookworm.application.AbstractUnitTest
import com.bookworm.application.customers.domain.model.{AuthenticationToken, AuthenticationTokenConfiguration, CustomerEmail, CustomerPassword}
import com.bookworm.application.customers.domain.port.inbound.command.AuthenticateCommand
import com.bookworm.application.customers.domain.port.outbound.{AuthenticationTokenRepository, CustomerRepository}

class CustomerAuthenticationUseCaseTest extends AbstractUnitTest {

  val customerRepository = mock[CustomerRepository[Id]]
  val authenticationTokenRepository = mock[AuthenticationTokenRepository[Id]]
  val jwtSecretKey = "secretKey"
  val tokenExpirationInSeconds = 10

  val customerAuthenticationUseCase =
    new CustomerAuthenticationUseCase[Id](
      customerRepository,
      authenticationTokenRepository,
      AuthenticationTokenConfiguration(jwtSecretKey, tokenExpirationInSeconds)
    )

  "CustomerAuthenticationUseCase" should {
    val authenticateCommand = AuthenticateCommand(customerEmail, customerPassword)

    "create an authentication token, delete the previous one and return the new one" in {
      (customerRepository
        .findBy(_: CustomerEmail, _: CustomerPassword))
        .expects(customerEmail, customerPassword)
        .returns(Some(registeredCustomerQueryModel))

      (authenticationTokenRepository.removeAuthenticationTokenOfCustomerId _).expects(customerId).returns(())
      (authenticationTokenRepository.saveAuthenticationToken _).expects(*).returns(())

      val actualAuthenticationToken = customerAuthenticationUseCase.login(authenticateCommand)

      actualAuthenticationToken.isDefined shouldBe true
    }

    "return None if the customer does not exist during login" in {
      (customerRepository
        .findBy(_: CustomerEmail, _: CustomerPassword))
        .expects(customerEmail, customerPassword)
        .returns(None)

      (authenticationTokenRepository.removeAuthenticationTokenOfCustomerId _).expects(customerId).never()
      (authenticationTokenRepository.saveAuthenticationToken _).expects(*).never()

      val actualAuthenticationToken = customerAuthenticationUseCase.login(authenticateCommand)

      actualAuthenticationToken.isDefined shouldBe false
    }

    "delete the current authentication token during logout" in {
      (authenticationTokenRepository.removeAuthenticationTokenOfCustomerId _).expects(customerId).returns(()).once()

      customerAuthenticationUseCase.logout(customerId)
    }

    "return true if the passed authentication token exists" in {
      val authenticationToken = AuthenticationToken(customerId, "token", tokenExpirationInSeconds)
      (authenticationTokenRepository.exists _).expects(authenticationToken).returns(true)

      customerAuthenticationUseCase.authenticate(authenticationToken) shouldBe true
    }

    "return false if the passed authentication token does not exist" in {
      val authenticationToken = AuthenticationToken(customerId, "token", tokenExpirationInSeconds)
      (authenticationTokenRepository.exists _).expects(authenticationToken).returns(false)

      customerAuthenticationUseCase.authenticate(authenticationToken) shouldBe false
    }
  }

}
