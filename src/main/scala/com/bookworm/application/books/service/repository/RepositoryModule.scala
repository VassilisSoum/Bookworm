package com.bookworm.application.books.service.repository

import com.google.inject.{AbstractModule, Scopes}
import net.codingwell.scalaguice.ScalaModule

class RepositoryModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[BookRepository].to[BookRepositoryImpl].in(Scopes.SINGLETON)
  }
}
