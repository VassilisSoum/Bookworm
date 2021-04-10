package com.bookworm.application.config.module

import com.bookworm.application.books.domain.port.inbound.{AddBookUseCase, GetBooksByGenreUseCase, RemoveBookUseCase, UpdateBookUseCase}
import com.google.inject.{AbstractModule, Scopes, TypeLiteral}
import doobie.ConnectionIO

class BooksUseCasesModule extends AbstractModule {

  override def configure(): Unit = {
    bind(new TypeLiteral[GetBooksByGenreUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
    bind(new TypeLiteral[AddBookUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
    bind(new TypeLiteral[RemoveBookUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
    bind(new TypeLiteral[UpdateBookUseCase[ConnectionIO]] {}).in(Scopes.SINGLETON)
  }
}
