package com.bookworm.application

import cats.effect.{ExitCode, IO, IOApp}
import com.bookworm.application.init.BookwormServer

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    BookwormServer.stream
}
