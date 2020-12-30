package com.bookworm.application.repository

import com.bookworm.application.repository.dao.{BookDao, BookDaoImpl}
import com.google.inject.{AbstractModule, Scopes}
import net.codingwell.scalaguice.ScalaModule

class RepositoryModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[BookDao].to[BookDaoImpl].in(Scopes.SINGLETON)
    bind[BookRepository].to[BookRepositoryImpl].in(Scopes.SINGLETON)
  }
}
