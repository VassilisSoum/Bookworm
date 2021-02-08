package com.bookworm.application.books.adapter.api

import cats.effect.Sync
import cats.implicits._
import com.bookworm.application.books.adapter.api.BookRestApi.createPaginationInfo
import com.bookworm.application.books.adapter.api.dto.BookResponseDto.BookResponseDtoOps
import com.bookworm.application.books.adapter.api.dto.ValidationErrorDto.ValidationErrorDtoOps
import com.bookworm.application.books.adapter.api.dto.{GetBooksResponseDto, ValidationErrorDto}
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.BookService
import org.http4s.{EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.json4s.jackson.jsonEncoderOf
import org.json4s.DefaultFormats

import javax.inject.Inject

class BookRestApi[F[_]: Sync] @Inject() (bookService: BookService[F]) extends Http4sDsl[F] {

  object OptionalLimitQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("limit")
  object OptionalContinuationTokenParamMatcher extends OptionalQueryParamDecoderMatcher[String]("continuationToken")

  implicit val formats = DefaultFormats

  implicit val validationErrorDtoEncoder: EntityEncoder[F, ValidationErrorDto] = jsonEncoderOf[F, ValidationErrorDto]

  implicit val getBooksResponseDtoEntityEncoder: EntityEncoder[F, GetBooksResponseDto] =
    jsonEncoderOf[F, GetBooksResponseDto]

  def getBooks: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "genre" / UUIDVar(genreId) / "books" :? OptionalContinuationTokenParamMatcher(
            maybeContinuationToken
          ) +& OptionalLimitQueryParamMatcher(maybeLimit) =>
        (maybeLimit match {
          case Some(limit) => createPaginationInfo(maybeContinuationToken, limit)
          case _           => createPaginationInfo(maybeContinuationToken, PaginationLimit.defaultPaginationLimit)
        }).fold(
          validationError => BadRequest(validationError.fromDomain),
          paginationInfo =>
            bookService.retrieveBooksByGenre(GenreId(genreId), paginationInfo).flatMap { booksByGenreQuery =>
              Ok(
                GetBooksResponseDto(
                  booksByGenreQuery.books.map(_.fromDomainQueryModel),
                  booksByGenreQuery.continuationToken.map(_.continuationToken)
                )
              )
            }
        )
    }
}

object BookRestApi {

  def createPaginationInfo(
    maybeContinuationToken: Option[String],
    limit: Int
  ): Either[ValidationError, PaginationInfo] =
    maybeContinuationToken match {
      case Some(continuationToken) =>
        for {
          paginationContinuationToken <- ContinuationToken.create(continuationToken)
          paginationLimit <- PaginationLimit.create(limit)
        } yield PaginationInfo(Some(paginationContinuationToken), paginationLimit)
      case None =>
        PaginationLimit.create(limit).map(paginationLimit => PaginationInfo(None, paginationLimit))
    }
}
