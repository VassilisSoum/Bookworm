package com.bookworm.application.customers.adapter.service

import cats.effect.IO
import com.bookworm.application.customers.adapter.service.model.{AuthenticationCustomerServiceModel, AuthenticationTerminationServiceModel}
import com.bookworm.application.customers.domain.model.AuthenticationToken
import com.bookworm.application.customers.domain.port.inbound.CustomerAuthenticationUseCase
import com.bookworm.application.customers.domain.port.inbound.command.AuthenticateCommand
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

import javax.inject.Inject

class CustomerAuthenticationApplicationService @Inject() (
    customerAuthenticationUseCase: CustomerAuthenticationUseCase[ConnectionIO],
    transactor: Transactor[IO]
) {

  def isCustomerLoggedIn(authenticationToken: AuthenticationToken): IO[Boolean] =
    customerAuthenticationUseCase.authenticate(authenticationToken).transact(transactor)

  def login(authenticationCustomerServiceModel: AuthenticationCustomerServiceModel): IO[Option[AuthenticationToken]] =
    customerAuthenticationUseCase
      .login(
        AuthenticateCommand(authenticationCustomerServiceModel.email, authenticationCustomerServiceModel.password)
      )
      .transact(transactor)

  def logout(authenticationTerminationServiceModel: AuthenticationTerminationServiceModel): IO[Unit] =
    customerAuthenticationUseCase.logout(authenticationTerminationServiceModel.customerId).transact(transactor)
}
