package com.bookworm.application.service

import com.google.inject.{AbstractModule, Scopes}
import net.codingwell.scalaguice.ScalaModule

class ServiceModule /*extends RepositoryModule*/ extends AbstractModule with ScalaModule {
  //lazy val bookService: BookService = wire[BookServiceImpl]

  override def configure(): Unit = {

    bind[BookService].to[BookServiceImpl].in(Scopes.SINGLETON)
  }

}
