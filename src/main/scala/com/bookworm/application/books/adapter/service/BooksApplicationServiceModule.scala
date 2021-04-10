package com.bookworm.application.books.adapter.service

import com.google.inject.{AbstractModule, Scopes}

class BooksApplicationServiceModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[BookApplicationService]).in(Scopes.SINGLETON)
    bind(classOf[AuthorApplicationService]).in(Scopes.SINGLETON)
  }
}
