package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.{BusinessErrorDto, GetAuthorsByBookIdResponseDto, GetBooksResponseDto, ValidationErrorDto}
import org.http4s.EntityDecoder
import org.http4s.json4s.jackson.jsonOf

trait EntityDecoders {
  implicit val validationErrorDtoEntityDecoder: EntityDecoder[IO, ValidationErrorDto] = jsonOf
  implicit val getBooksResponseEntityDecoder: EntityDecoder[IO, GetBooksResponseDto] = jsonOf
  implicit val businessErrorDtoEntityDecoder: EntityDecoder[IO, BusinessErrorDto] = jsonOf
  implicit val getAuthorsByBookIdResponseDtoDecoder: EntityDecoder[IO, GetAuthorsByBookIdResponseDto] = jsonOf
}
