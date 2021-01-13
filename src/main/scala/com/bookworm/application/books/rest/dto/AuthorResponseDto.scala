package com.bookworm.application.books.rest.dto

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

import java.util.UUID

case class AuthorResponseDto(authorId: UUID, firstName: String, lastName: String, books: List[BookResponseDto])

object AuthorResponseDto {
  implicit val encoder: Encoder[AuthorResponseDto] = deriveEncoder[AuthorResponseDto]
  implicit val decoder: Decoder[AuthorResponseDto] = deriveDecoder[AuthorResponseDto]
}
