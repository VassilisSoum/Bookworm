package com.bookworm.application.books.adapter.api

import cats.effect.IO
import com.bookworm.application.books.adapter.api.BookRestApi.createPaginationInfo
import com.bookworm.application.books.adapter.api.dto.AddBookRequestDto._
import com.bookworm.application.books.adapter.api.dto.BookResponseDto.BookResponseDtoOps
import com.bookworm.application.books.adapter.api.dto._
import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.{AddBookUseCase, GetBooksByGenreUseCase, RemoveBookUseCase, UpdateBookUseCase}
import org.http4s.dsl.Http4sDsl
import org.http4s.json4s.jackson.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}

import javax.inject.Inject

class BookRestApi @Inject() (
    getBooksByGenreUseCase: GetBooksByGenreUseCase[IO],
    addBookUseCase: AddBookUseCase[IO],
    removeBookUseCase: RemoveBookUseCase[IO],
    updateBookUseCase: UpdateBookUseCase[IO]
) extends Http4sDsl[IO] {

  object OptionalLimitQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Int]("limit")
  object OptionalContinuationTokenParamMatcher extends OptionalQueryParamDecoderMatcher[String]("continuationToken")

  implicit val validationErrorDtoEncoder: EntityEncoder[IO, ValidationErrorDto] = jsonEncoderOf[IO, ValidationErrorDto]

  implicit val businessErrorDtoEncoder: EntityEncoder[IO, BusinessErrorDto] = jsonEncoderOf[IO, BusinessErrorDto]

  implicit val getBooksResponseDtoEntityEncoder: EntityEncoder[IO, GetBooksResponseDto] =
    jsonEncoderOf[IO, GetBooksResponseDto]

  implicit val createBookRequestDtoDecoder: EntityDecoder[IO, AddBookRequestDto] = jsonOf[IO, AddBookRequestDto]

  implicit val updateBookRequestDtoDecoder: EntityDecoder[IO, UpdateBookRequestDto] = jsonOf[IO, UpdateBookRequestDto]

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
            getBooksByGenreUseCase.retrieveBooksByGenre(GenreId(genreId), paginationInfo).flatMap { booksByGenreQuery =>
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
              addBookUseCase.addBook(book).flatMap {
                case Left(businessError) => Conflict(BusinessErrorDto.fromDomain(businessError))
                case Right(_)            => NoContent()
              }
          )
        }
      case DELETE -> Root / "books" / UUIDVar(bookId) =>
        removeBookUseCase.removeBook(BookId(bookId)).flatMap {
          case Left(businessError) => Conflict(BusinessErrorDto.fromDomain(businessError))
          case Right(_)            => NoContent()
        }
      case req @ PUT -> Root / "books" / UUIDVar(bookId) =>
        req.as[UpdateBookRequestDto].flatMap { updateBookRequestDto =>
          updateBookRequestDto
            .toDomainModel(BookId(bookId))
            .fold(
              validationError => BadRequest(ValidationErrorDto.fromDomain(validationError)),
              book =>
                updateBookUseCase.updateBook(book).flatMap {
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
