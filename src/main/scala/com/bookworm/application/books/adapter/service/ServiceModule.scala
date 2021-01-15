package com.bookworm.application.books.adapter.service

import cats.effect.IO
import com.bookworm.application.books.domain.port.inbound.BookService
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import net.codingwell.scalaguice.ScalaModule

class ServiceModule extends AbstractModule with ScalaModule {

  override def configure(): Unit =
    bind(new TypeLiteral[BookService[IO]] {}).to(new TypeLiteral[BookServiceImpl] {}).in(Scopes.SINGLETON)

}
