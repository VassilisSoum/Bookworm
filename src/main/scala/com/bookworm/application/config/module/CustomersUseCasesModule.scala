package com.bookworm.application.config.module

import com.bookworm.application.customers.domain.port.inbound.{RegisterCustomerUseCase, RetrieveCustomerDetailsUseCase, VerificationTokenUseCase}
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import doobie.ConnectionIO

class CustomersUseCasesModule extends AbstractModule {

  override def configure(): Unit = {
    bind(new TypeLiteral[RegisterCustomerUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
    bind(new TypeLiteral[VerificationTokenUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
    bind(new TypeLiteral[RetrieveCustomerDetailsUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
  }
}
