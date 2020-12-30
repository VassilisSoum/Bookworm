package com.bookworm.application.rest

import com.google.inject.{AbstractModule, Scopes}
import net.codingwell.scalaguice.ScalaModule

class RestModule /*extends ServiceModule*/ extends AbstractModule with ScalaModule {
  /*lazy val bookRestApi: BookRestApi = wire[BookRestApi]*/

  override def configure(): Unit = {

    bind[BookRestApi].in(Scopes.SINGLETON)
  }
}
