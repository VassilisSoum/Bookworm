package com.bookworm.application.integration.books

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.AddBookRequestDto
import org.http4s.EntityEncoder
import org.http4s.json4s.jackson.jsonEncoderOf

trait EntityEncoders {
  implicit val createBookRequestDtoEncoder: EntityEncoder[IO, AddBookRequestDto] =
    jsonEncoderOf[IO, AddBookRequestDto]
}
