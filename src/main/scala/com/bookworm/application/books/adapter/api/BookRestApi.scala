package com.bookworm.application.books.adapter.api

import cats.effect.Sync
import cats.implicits._
import com.bookworm.application.books.adapter.api.BookRestApi.createPaginationInfo
import com.bookworm.application.books.adapter.api.dto.BookResponseDto.BookResponseDtoOps
import com.bookworm.application.books.adapter.api.dto.CreateBookRequestDto._
import com.bookworm.application.books.adapter.api.dto.{BusinessErrorDto, CreateBookRequestDto, GetBooksResponseDto, ValidationErrorDto}
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.BookService
import org.http4s.dsl.Http4sDsl
import org.http4s.json4s.jackson.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

import javax.inject.Inject

class BookRestApi[F[_]: Sync] @Inject() (bookService: BookService[F]) extends Http4sDsl[F] {

  object OptionalLimitQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("limit")
  object OptionalContinuationTokenParamMatcher extends OptionalQueryParamDecoderMatcher[String]("continuationToken")

  implicit val validationErrorDtoEncoder: EntityEncoder[F, ValidationErrorDto] = jsonEncoderOf[F, ValidationErrorDto]

  implicit val businessErrorDtoEncoder: EntityEncoder[F, BusinessErrorDto] = jsonEncoderOf[F, BusinessErrorDto]

  implicit val getBooksResponseDtoEntityEncoder: EntityEncoder[F, GetBooksResponseDto] =
    jsonEncoderOf[F, GetBooksResponseDto]

  implicit val createBookRequestDtoDecoder: EntityDecoder[F, CreateBookRequestDto] = jsonOf[F, CreateBookRequestDto]

  def getBooks: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root / "genre" / UUIDVar(genreId) / "books" :? OptionalContinuationTokenParamMatcher(
            maybeContinuationToken
          ) +& OptionalLimitQueryParamMatcher(maybeLimit) =>
        (maybeLimit match {
          case Some(limit) => createPaginationInfo(maybeContinuationToken, limit)
          case _           => createPaginationInfo(maybeContinuationToken, PaginationLimit.defaultPaginationLimit)
        }).fold(
          validationError => BadRequest(ValidationErrorDto.fromDomain(validationError)),
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
      case req @ POST -> Root / "books" =>
        req.as[CreateBookRequestDto].flatMap { createBookRequestDto =>
          createBookRequestDto.toDomainModel.fold(
            validationError => BadRequest(ValidationErrorDto.fromDomain(validationError)),
            book =>
              bookService.createBook(book).flatMap {
                case Left(businessError) => Conflict(BusinessErrorDto.fromDomain(businessError))
                case Right(_)            => NoContent()
              }
          )
        }

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
