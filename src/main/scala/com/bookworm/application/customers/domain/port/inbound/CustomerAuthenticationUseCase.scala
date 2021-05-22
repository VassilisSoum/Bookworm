package com.bookworm.application.customers.domain.port.inbound

import cats.Monad
import cats.arrow.FunctionK
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

class CustomerAuthenticationUseCase[F[_]: Monad, G[_]: Monad] @Inject() ( //G is ConnectionIO
    customerRepository: CustomerRepository[G],
    authenticationTokenRepository: AuthenticationTokenRepository[F],
    authenticationTokenConfiguration: AuthenticationTokenConfiguration,
    liftToG: FunctionK[F, G]
) {

  def authenticate(authenticationToken: AuthenticationToken): G[Boolean] =
    liftToG(authenticationTokenRepository.exists(authenticationToken))

  def login(authenticateCommand: AuthenticateCommand): G[Option[AuthenticationToken]] =
    customerRepository.findBy(authenticateCommand.email, authenticateCommand.password).flatMap {
      case Some(customerQueryModel) =>
        logout(CustomerId(customerQueryModel.customerId))
          .flatMap(_ =>
            liftToG(
              Monad[F]
                .pure(createAuthenticationToken(customerQueryModel.customerId))
                .flatMap(authenticationToken =>
                  authenticationTokenRepository
                    .saveAuthenticationToken(authenticationToken)
                    .map(_ => Some(authenticationToken))
                )
            )
          )
      case None => liftToG(Monad[F].pure(None))
    }

  def logout(customerId: CustomerId): G[Unit] =
    liftToG(authenticationTokenRepository.removeAuthenticationTokenOfCustomerId(customerId))

  private def createAuthenticationToken(customerId: UUID): AuthenticationToken = {
    val claim = JObject() ~ ("customerId" -> customerId.toString) ~ ("role" -> role)

    val token = JwtJson4s.encode(
      claim,
      authenticationTokenConfiguration.jwtTokenSecretKeyEncryption,
      authenticationTokenConfiguration.algorithm
    )
    AuthenticationToken(CustomerId(customerId), token, authenticationTokenConfiguration.tokenExpirationInSeconds)

  }
}

object CustomerAuthenticationUseCase {
  private val role = "CUSTOMER"
}
