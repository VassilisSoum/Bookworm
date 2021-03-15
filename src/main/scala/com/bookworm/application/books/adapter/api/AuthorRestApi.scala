package com.bookworm.application.books.adapter.api

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.AuthorResponseDto.AuthorResponseDtoOps
import com.bookworm.application.books.adapter.api.dto.{AuthorResponseDto, BusinessErrorDto, GetAuthorsByBookIdResponseDto}
import com.bookworm.application.books.adapter.service.AuthorApplicationService
import com.bookworm.application.books.domain.model.BookId
import com.bookworm.application.books.domain.model.DomainBusinessError.BookDoesNotExist
import org.http4s.dsl.Http4sDsl
import org.http4s.json4s.jackson.jsonEncoderOf
import org.http4s.{EntityEncoder, HttpRoutes}

import javax.inject.Inject

class AuthorRestApi @Inject() (authorApplicationService: AuthorApplicationService) extends Http4sDsl[IO] {

  implicit private val getAuthorsByBookIdResponseDtoEntityEntity: EntityEncoder[IO, GetAuthorsByBookIdResponseDto] =
    jsonEncoderOf[IO, GetAuthorsByBookIdResponseDto]

  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root / "authors" / "book" / UUIDVar(bookId) =>
      authorApplicationService.retrieveAuthorsByBookId(BookId(bookId)).flatMap {
        case Left(businessError) =>
          BusinessErrorDto.fromDomain(businessError) match {
            case businessErrorDto @ BusinessErrorDto(BookDoesNotExist, _) => NotFound(businessErrorDto)
            case businessErrorDto @ BusinessErrorDto(_, _)                => InternalServerError(businessErrorDto)
          }
        case Right(authorsByBookIdQuery) =>
          val authors: List[AuthorResponseDto] = authorsByBookIdQuery.authors.map(_.fromDomainQueryModel)
          Ok(GetAuthorsByBookIdResponseDto(authors))
      }
    }
}
