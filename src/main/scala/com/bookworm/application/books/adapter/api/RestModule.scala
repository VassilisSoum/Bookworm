package com.bookworm.application.books.adapter.api

import cats.effect.Async
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import net.codingwell.scalaguice.ScalaModule

class RestModule[F[_]: Async] extends AbstractModule with ScalaModule {

  override def configure(): Unit =
    bind(new TypeLiteral[BookRestApi[F]] {}).in(Scopes.SINGLETON)
}
