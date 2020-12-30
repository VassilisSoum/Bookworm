package com.bookworm.application.rest

import cats.effect.IO
import com.bookworm.application.rest.dto.BookDto
import com.bookworm.application.service.BookService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl

import javax.inject.Inject

class BookRestApi @Inject() (bookwormService: BookService) extends Http4sDsl[IO] {

  def getAllBooks: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root / "books" =>
      bookwormService.retrieveAllBooks.flatMap { books =>
        Ok(books.map(BookDto.fromBook))
      }
    }
}
