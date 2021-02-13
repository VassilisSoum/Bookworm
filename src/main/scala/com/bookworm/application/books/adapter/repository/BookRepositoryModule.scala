package com.bookworm.application.books.adapter.repository

import cats.effect.Sync
import com.bookworm.application.books.domain.port.outbound.BookRepository
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import net.codingwell.scalaguice.ScalaModule

class BookRepositoryModule[F[_]: Sync] extends AbstractModule with ScalaModule {

  override def configure(): Unit =
    bind(new TypeLiteral[BookRepository[F]]() {})
      .to(new TypeLiteral[BookRepositoryImpl[F]]() {})
      .in(Scopes.SINGLETON)
}
