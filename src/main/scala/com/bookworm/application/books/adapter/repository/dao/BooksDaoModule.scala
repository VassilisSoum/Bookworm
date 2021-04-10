package com.bookworm.application.books.adapter.repository.dao

import com.google.inject.{AbstractModule, Scopes}

class BooksDaoModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[BookDao]).in(Scopes.SINGLETON)
    bind(classOf[AuthorDao]).in(Scopes.SINGLETON)
  }
}
