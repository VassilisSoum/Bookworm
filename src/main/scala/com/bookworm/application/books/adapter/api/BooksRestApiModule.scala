package com.bookworm.application.books.adapter.api

import com.google.inject.{AbstractModule, Scopes}

class BooksRestApiModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[BookRestApi]).in(Scopes.SINGLETON)
    bind(classOf[AuthorRestApi]).in(Scopes.SINGLETON)
  }
}
