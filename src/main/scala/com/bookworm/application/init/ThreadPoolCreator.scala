package com.bookworm.application.init

import cats.effect.{IO, Resource}
import doobie.util.ExecutionContexts

import scala.concurrent.ExecutionContext

object ThreadPoolCreator {
  def createFixedThreadPool(size: Int): Resource[IO, ExecutionContext] = {
    ExecutionContexts.fixedThreadPool[IO](size)
  }
}
