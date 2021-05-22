package com.bookworm.application.config.module

import cats.effect.IO
import com.bookworm.application.config.Configuration.AuthenticationTokensConfig
import com.bookworm.application.customers.domain.model.AuthenticationTokenConfiguration
import com.bookworm.application.customers.domain.port.inbound.{CustomerAuthenticationUseCase, RegisterCustomerUseCase, RetrieveCustomerDetailsUseCase, VerificationTokenUseCase}
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import doobie.ConnectionIO

class CustomersUseCasesModule(authenticationTokensConfig: AuthenticationTokensConfig) extends AbstractModule {

  override def configure(): Unit = {
    bind(new TypeLiteral[RegisterCustomerUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
    bind(new TypeLiteral[VerificationTokenUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
    bind(new TypeLiteral[RetrieveCustomerDetailsUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
    bind(new TypeLiteral[CustomerAuthenticationUseCase[IO, ConnectionIO]] {}).in(Scopes.SINGLETON)
    bind(classOf[AuthenticationTokenConfiguration]).toInstance(
      AuthenticationTokenConfiguration(
        authenticationTokensConfig.jwtSecretKey,
        authenticationTokensConfig.expirationInSeconds
      )
    )
  }
}
