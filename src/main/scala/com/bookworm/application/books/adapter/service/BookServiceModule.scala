package com.bookworm.application.books.adapter.service

import cats.effect.Sync
import com.bookworm.application.books.domain.port.inbound.BookService
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import net.codingwell.scalaguice.ScalaModule

class BookServiceModule[F[_]: Sync] extends AbstractModule with ScalaModule {

  override def configure(): Unit =
    bind(new TypeLiteral[BookService[F]]() {})
      .to(new TypeLiteral[BookServiceImpl[F]]() {})
      .in(Scopes.SINGLETON)

}
