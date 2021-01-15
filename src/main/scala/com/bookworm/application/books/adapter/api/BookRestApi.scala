package com.bookworm.application.books.adapter.api

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.BookAndAuthorResponseDto.BookAndAuthorResponseDtoOps
import com.bookworm.application.books.domain.model.GenreId
import com.bookworm.application.books.domain.port.inbound.BookService
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import javax.inject.Inject

class BookRestApi @Inject() (bookService: BookService[IO]) extends Http4sDsl[IO] {

  def getAllBooks: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root / "genre" / UUIDVar(genreId) / "books" =>
      bookService.retrieveAllBooksByGenre(GenreId(genreId)).flatMap { books =>
        Ok(
          books.map({ case (bookId, bookWithAuthorList) => bookId.id -> bookWithAuthorList.map(_.fromDomain) })
        )
      }
    }
}
