package com.bookworm.application.integration.books

import cats.data.Kleisli
import cats.effect.IO
import com.bookworm.application.IntegrationTestModule
import com.bookworm.application.books.adapter.api.AuthorRestApi
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.{Request, Response}

trait AuthorEndpoints { integrationTestModule: IntegrationTestModule =>

  val endpoint: Kleisli[IO, Request[IO], Response[IO]] =
    Router("/" -> injector.getInstance(classOf[AuthorRestApi]).routes).orNotFound

}
