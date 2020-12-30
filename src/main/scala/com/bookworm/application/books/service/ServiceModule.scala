package com.bookworm.application.books.service

import com.google.inject.{AbstractModule, Scopes}
import net.codingwell.scalaguice.ScalaModule

class ServiceModule extends AbstractModule with ScalaModule {

  override def configure(): Unit =
    bind[BookService].to[BookServiceImpl].in(Scopes.SINGLETON)

}
