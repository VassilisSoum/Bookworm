package com.bookworm.application.books.adapter.repository

import cats.effect.IO
import com.bookworm.application.books.domain.port.outbound.BookRepository
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import net.codingwell.scalaguice.ScalaModule

class RepositoryModule extends AbstractModule with ScalaModule {

  override def configure(): Unit =
    bind(new TypeLiteral[BookRepository[IO]] {}).to(classOf[BookRepositoryImpl]).in(Scopes.SINGLETON)
}
