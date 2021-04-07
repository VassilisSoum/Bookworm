package com.bookworm.application.integration.customers

import cats.data.Kleisli
import cats.effect.IO
import com.bookworm.application.IntegrationTestModule
import com.bookworm.application.customers.adapter.api.CustomerRegistrationRestApi
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.{Request, Response}

trait CustomerEndpoints { integrationTestModule: IntegrationTestModule =>

  val endpoint: Kleisli[IO, Request[IO], Response[IO]] =
    Router("/" -> injector.getInstance(classOf[CustomerRegistrationRestApi]).routes).orNotFound

}
