package com.bookworm.application.customers.domain.port.inbound

import cats.Monad
import cats.implicits._
import com.bookworm.application.customers.domain.model.{AuthenticationToken, AuthenticationTokenConfiguration, CustomerId}
import com.bookworm.application.customers.domain.port.inbound.CustomerAuthenticationUseCase.role
import com.bookworm.application.customers.domain.port.inbound.command.AuthenticateCommand
import com.bookworm.application.customers.domain.port.outbound.{AuthenticationTokenRepository, CustomerRepository}
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import pdi.jwt.JwtJson4s

import java.util.UUID
import javax.inject.Inject

class CustomerAuthenticationUseCase[F[_]: Monad] @Inject()(
    customerRepository: CustomerRepository[F],
    authenticationTokenRepository: AuthenticationTokenRepository[F],
    authenticationTokenConfiguration: AuthenticationTokenConfiguration
) {

  def authenticate(authenticationToken: AuthenticationToken): F[Boolean] =
    authenticationTokenRepository.exists(authenticationToken)

  def login(authenticateCommand: AuthenticateCommand): F[Option[AuthenticationToken]] =
    customerRepository.findBy(authenticateCommand.email, authenticateCommand.password).flatMap {
      case Some(customerQueryModel) =>
        logout(CustomerId(customerQueryModel.customerId))
          .flatMap(_ =>
            createAuthenticationToken(customerQueryModel.customerId).flatMap(authenticationToken =>
              authenticationTokenRepository
                .saveAuthenticationToken(authenticationToken)
                .map(_ => Some(authenticationToken))
            )
          )
      case None => Monad[F].pure(None)
    }

  def logout(customerId: CustomerId): F[Unit] =
    authenticationTokenRepository.removeAuthenticationTokenOfCustomerId(customerId)

  private def createAuthenticationToken(customerId: UUID): F[AuthenticationToken] = {
    val claim = JObject() ~ ("customerId" -> customerId.toString) ~ ("role" -> role)

    val token = JwtJson4s.encode(
      claim,
      authenticationTokenConfiguration.jwtTokenSecretKeyEncryption,
      authenticationTokenConfiguration.algorithm
    )

    Monad[F].pure(
      AuthenticationToken(CustomerId(customerId), token, authenticationTokenConfiguration.tokenExpirationInSeconds)
    )
  }
}

object CustomerAuthenticationUseCase {
  private val role = "CUSTOMER"
}
