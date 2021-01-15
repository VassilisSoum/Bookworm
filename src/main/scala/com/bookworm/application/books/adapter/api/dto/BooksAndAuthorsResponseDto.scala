package com.bookworm.application.books.adapter.api.dto

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

import java.util.UUID

case class BooksAndAuthorsResponseDto(result: Map[UUID, List[BookAndAuthorResponseDto]])

object BooksAndAuthorsResponseDto {
  implicit val encoder: Encoder[BooksAndAuthorsResponseDto] = deriveEncoder[BooksAndAuthorsResponseDto]
  implicit val decoder: Decoder[BooksAndAuthorsResponseDto] = deriveDecoder[BooksAndAuthorsResponseDto]
}
