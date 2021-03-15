package com.bookworm.application.books.adapter.api

import cats.effect.IO
import com.bookworm.application.books.adapter.api.BookRestApi.createPaginationInfo
import com.bookworm.application.books.adapter.api.dto.AddBookRequestDto._
import com.bookworm.application.books.adapter.api.dto.BookResponseDto.BookResponseDtoOps
import com.bookworm.application.books.adapter.api.dto._
import com.bookworm.application.books.adapter.service.BookApplicationService
import com.bookworm.application.books.domain.model.DomainBusinessError.{BookDoesNotExist, OneOrMoreAuthorsDoNotExist}
import com.bookworm.application.books.domain.model._
import org.http4s.dsl.Http4sDsl
import org.http4s.json4s.jackson.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

import javax.inject.Inject

class BookRestApi @Inject() (
    bookApplicationService: BookApplicationService
) extends Http4sDsl[IO] {

  private object OptionalLimitQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("limit")

  private object OptionalContinuationTokenParamMatcher
    extends OptionalQueryParamDecoderMatcher[String]("continuationToken")

  implicit private val getBooksResponseDtoEntityEncoder: EntityEncoder[IO, GetBooksResponseDto] =
    jsonEncoderOf[IO, GetBooksResponseDto]

  implicit private val createBookRequestDtoDecoder: EntityDecoder[IO, AddBookRequestDto] = jsonOf[IO, AddBookRequestDto]

  implicit private val updateBookRequestDtoDecoder: EntityDecoder[IO, UpdateBookRequestDto] =
    jsonOf[IO, UpdateBookRequestDto]

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
            bookApplicationService.retrieveBooksByGenre(GenreId(genreId), paginationInfo).flatMap { booksByGenreQuery =>
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
              bookApplicationService.addBook(book).flatMap {
                case Left(businessError) => Conflict(BusinessErrorDto.fromDomain(businessError))
                case Right(_)            => NoContent()
              }
          )
        }
      case DELETE -> Root / "books" / UUIDVar(bookId) =>
        bookApplicationService.removeBook(BookId(bookId)).flatMap {
          case Left(businessError) =>
            BusinessErrorDto.fromDomain(businessError) match {
              case businessErrorDto @ BusinessErrorDto(BookDoesNotExist, _) => NotFound(businessErrorDto)
              case businessErrorDto @ BusinessErrorDto(_, _)                => InternalServerError(businessErrorDto)
            }
          case Right(_) => NoContent()
        }
      case req @ PUT -> Root / "books" / UUIDVar(bookId) =>
        req.as[UpdateBookRequestDto].flatMap { updateBookRequestDto =>
          updateBookRequestDto
            .toDomainModel(BookId(bookId))
            .fold(
              validationError => BadRequest(ValidationErrorDto.fromDomain(validationError)),
              book =>
                bookApplicationService.updateBook(book).flatMap {
                  case Left(businessError) =>
                    BusinessErrorDto.fromDomain(businessError) match {
                      case businessErrorDto @ BusinessErrorDto(OneOrMoreAuthorsDoNotExist, _) =>
                        Conflict(businessErrorDto)
                      case businessErrorDto @ BusinessErrorDto(BookDoesNotExist, _) => NotFound(businessErrorDto)
                    }
                  case Right(_) => NoContent()
                }
            )
        }
    }
}

object BookRestApi {

  def createPaginationInfo(
    maybeContinuationToken: Option[String],
    limit: Int
  ): Either[DomainValidationError, PaginationInfo] =
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
