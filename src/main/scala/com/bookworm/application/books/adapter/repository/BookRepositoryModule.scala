package com.bookworm.application.books.adapter.repository

import com.bookworm.application.books.domain.port.outbound.{AuthorRepository, BookRepository}
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import doobie.ConnectionIO
import net.codingwell.scalaguice.ScalaModule

class BookRepositoryModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind(new TypeLiteral[BookRepository[ConnectionIO]]() {})
      .to(new TypeLiteral[BookRepositoryImpl]() {})
      .in(Scopes.SINGLETON)

    bind(new TypeLiteral[AuthorRepository[ConnectionIO]]() {})
      .to(new TypeLiteral[AuthorRepositoryImpl]() {})
      .in(Scopes.SINGLETON)
  }
}
