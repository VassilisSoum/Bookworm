package com.bookworm.application.books.adapter

import cats.effect.IO
import com.bookworm.application.books.adapter.api.dto.BusinessErrorDto.BusinessErrorSerializer
import com.bookworm.application.books.adapter.api.dto.{BusinessErrorDto, ValidationErrorDto}
import com.bookworm.application.books.adapter.api.dto.ValidationErrorDto.ValidationErrorSerializer
import org.http4s.EntityEncoder
import org.http4s.json4s.jackson.jsonEncoderOf
import org.json4s.ext.JavaTypesSerializers
import org.json4s.{DefaultFormats, Formats}

package object api {

  implicit val formats: Formats =
    DefaultFormats ++ JavaTypesSerializers.all + ValidationErrorSerializer + BusinessErrorSerializer

  implicit val validationErrorDtoEncoder: EntityEncoder[IO, ValidationErrorDto] = jsonEncoderOf[IO, ValidationErrorDto]

  implicit val businessErrorDtoEncoder: EntityEncoder[IO, BusinessErrorDto] = jsonEncoderOf[IO, BusinessErrorDto]

}
