package com.bookworm.application.integration.customers

import cats.effect.IO
import com.bookworm.application.customers.adapter.api.dto.{CompleteCustomerRegistrationRequestDto, CustomerRegistrationRequestDto}
import org.http4s.EntityEncoder
import org.http4s.json4s.jackson.jsonEncoderOf

trait EntityEncoders {

  implicit val customerRegistrationRequestDtoEntityEncoder: EntityEncoder[IO, CustomerRegistrationRequestDto] =
    jsonEncoderOf[IO, CustomerRegistrationRequestDto]

  implicit val completeCustomerRegistrationRequestDtoEntityEncoder
    : EntityEncoder[IO, CompleteCustomerRegistrationRequestDto] =
    jsonEncoderOf[IO, CompleteCustomerRegistrationRequestDto]
}
