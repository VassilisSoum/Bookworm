package com.bookworm.application.integration.customers

import cats.effect.IO
import com.bookworm.application.customers.adapter.api.dto.{BusinessErrorDto, ValidationErrorDto}
import org.http4s.EntityDecoder
import org.http4s.json4s.jackson.jsonOf

trait EntityDecoders {
  implicit val validationErrorDtoEntityDecoder: EntityDecoder[IO, ValidationErrorDto] = jsonOf
  implicit val businessErrorDtoEntityDecoder: EntityDecoder[IO, BusinessErrorDto] = jsonOf
}
