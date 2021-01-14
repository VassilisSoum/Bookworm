package com.bookworm.application.books.rest

import cats.effect.IO
import com.bookworm.application.books.rest.dto.BookResponseDto
import com.bookworm.application.books.service.BookService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl

import javax.inject.Inject

class BookRestApi @Inject() (bookwormService: BookService) extends Http4sDsl[IO] {

  def getAllBooks: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root / "genre" / UUIDVar(genreId) / "books" =>
      bookwormService.retrieveAllBooks(genreId).flatMap { books =>
        Ok(books.map(BookResponseDto.from))
      }
    }
}
