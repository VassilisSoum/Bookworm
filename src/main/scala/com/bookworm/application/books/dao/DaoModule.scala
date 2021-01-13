package com.bookworm.application.books.dao

import com.google.inject.{AbstractModule, Scopes}

class DaoModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[BookDao]).to(classOf[BookDaoImpl]).in(Scopes.SINGLETON)
  }
}
