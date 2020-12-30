package com.bookworm.application.rest

import com.google.inject.{AbstractModule, Scopes}
import net.codingwell.scalaguice.ScalaModule

class RestModule extends AbstractModule with ScalaModule {

  override def configure(): Unit =
    bind[BookRestApi].in(Scopes.SINGLETON)
}
