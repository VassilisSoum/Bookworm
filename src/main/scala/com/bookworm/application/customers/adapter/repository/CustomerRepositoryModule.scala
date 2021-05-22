package com.bookworm.application.customers.adapter.repository

import cats.effect.IO
import com.bookworm.application.customers.domain.port.outbound.{AuthenticationTokenRepository, CustomerEmailTemplateRepository, CustomerRepository, VerificationTokenRepository}
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import doobie.ConnectionIO
import net.codingwell.scalaguice.ScalaModule

class CustomerRepositoryModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind(new TypeLiteral[CustomerRepository[ConnectionIO]]() {})
      .to(new TypeLiteral[CustomerRepositoryImpl]() {})
      .in(Scopes.SINGLETON)

    bind(new TypeLiteral[VerificationTokenRepository[ConnectionIO]]() {})
      .to(new TypeLiteral[VerificationTokenRepositoryImpl]() {})
      .in(Scopes.SINGLETON)

    bind(new TypeLiteral[CustomerEmailTemplateRepository[ConnectionIO]]() {})
      .to(new TypeLiteral[CustomerEmailTemplateRepositoryImpl]() {})
      .in(Scopes.SINGLETON)

    bind(new TypeLiteral[AuthenticationTokenRepository[IO]]() {})
      .to(new TypeLiteral[AuthenticationTokenRepositoryImpl]() {})
      .in(Scopes.SINGLETON)
  }
}
