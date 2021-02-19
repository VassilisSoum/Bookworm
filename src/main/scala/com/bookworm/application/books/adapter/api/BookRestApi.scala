package com.bookworm.application.books.adapter.api

import cats.effect.IO
import com.bookworm.application.books.adapter.api.BookRestApi.createPaginationInfo
import com.bookworm.application.books.adapter.api.dto.BookResponseDto.BookResponseDtoOps
import com.bookworm.application.books.adapter.api.dto.AddBookRequestDto._
import com.bookworm.application.books.adapter.api.dto.{BusinessErrorDto, AddBookRequestDto, GetBooksResponseDto, ValidationErrorDto}
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.BookService
import org.http4s.dsl.Http4sDsl
import org.http4s.json4s.jackson.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

import javax.inject.Inject

class BookRestApi @Inject() (bookService: BookService) extends Http4sDsl[IO] {

  object OptionalLimitQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("limit")
  object OptionalContinuationTokenParamMatcher extends OptionalQueryParamDecoderMatcher[String]("continuationToken")

  implicit val validationErrorDtoEncoder: EntityEncoder[IO, ValidationErrorDto] = jsonEncoderOf[IO, ValidationErrorDto]

  implicit val businessErrorDtoEncoder: EntityEncoder[IO, BusinessErrorDto] = jsonEncoderOf[IO, BusinessErrorDto]

  implicit val getBooksResponseDtoEntityEncoder: EntityEncoder[IO, GetBooksResponseDto] =
    jsonEncoderOf[IO, GetBooksResponseDto]

  implicit val createBookRequestDtoDecoder: EntityDecoder[IO, AddBookRequestDto] = jsonOf[IO, AddBookRequestDto]

  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
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
        req.as[AddBookRequestDto].flatMap { addBookRequestDto =>
          addBookRequestDto.toDomainModel.fold(
            validationError => BadRequest(ValidationErrorDto.fromDomain(validationError)),
            book =>
              bookService.addBook(book).flatMap {
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
