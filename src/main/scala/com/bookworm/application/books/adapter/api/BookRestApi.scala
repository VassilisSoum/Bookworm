package com.bookworm.application.books.adapter.api

import cats.effect.Sync
import cats.implicits._
import com.bookworm.application.books.adapter.api.dto.BookAndAuthorResponseDto.BookAndAuthorResponseDtoOps
import com.bookworm.application.books.domain.model.GenreId
import com.bookworm.application.books.domain.port.inbound.BookService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl

import javax.inject.Inject

class BookRestApi[F[_]: Sync] @Inject() (bookService: BookService[F]) extends Http4sDsl[F] {

  def getAllBooks: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / "genre" / UUIDVar(genreId) / "books" =>
      bookService.retrieveAllBooksByGenre(GenreId(genreId)).flatMap { books =>
        Ok(
          books.map({ case (bookId, bookWithAuthorList) => bookId.id -> bookWithAuthorList.map(_.fromDomain) })
        )
      }
    }
}
