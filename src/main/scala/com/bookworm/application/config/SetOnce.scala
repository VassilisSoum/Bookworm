package com.bookworm.application.config

import cats.effect._
import cats.effect.concurrent.{Deferred, Ref}
import cats.implicits._

import scala.concurrent.ExecutionContext

trait SetOnce[+A] {
  def get: IO[A]
}

object SetOnce {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def create[A](fa: IO[A]): IO[SetOnce[A]] = {
    sealed trait State
    case class Value(v: A) extends State
    case class Updating(d: Deferred[IO, Either[Throwable, A]]) extends State
    case object NoValue extends State

    Ref.of[IO, State](NoValue).map { state =>
      new SetOnce[A] {

        def get: IO[A] = Deferred[IO, Either[Throwable, A]].flatMap { newValue =>
          state.modify {
            case st @ Value(v) =>
              st -> v.pure[IO]
            case st @ Updating(inFlightValue) =>
              st -> inFlightValue.get.rethrow
            case NoValue =>
              Updating(newValue) -> fetch(newValue).rethrow
          }.flatten
        }

        def fetch(d: Deferred[IO, Either[Throwable, A]]): IO[Either[Throwable, A]] = {
          for {
            r <- fa.attempt
            _ <- state.set {
              r match {
                case Left(_)  => NoValue
                case Right(v) => Value(v)
              }
            }
            _ <- d.complete(r)
          } yield r
        }.guaranteeCase {
          case ExitCase.Completed => ().pure[IO]
          case ExitCase.Error(_)  => ().pure[IO]
          case ExitCase.Canceled =>
            state.modify {
              case st @ Value(v) => st -> d.complete(v.asRight).attempt.void
              case NoValue | Updating(_) =>
                val error = new Exception("Couldn't retrieve Transactor")
                NoValue -> d.complete(error.asLeft).attempt.void
            }.flatten

        }
      }
    }
  }
}
