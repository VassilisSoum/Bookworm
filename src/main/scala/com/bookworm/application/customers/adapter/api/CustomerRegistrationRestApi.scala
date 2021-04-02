package com.bookworm.application.customers.adapter.api

import cats.effect.IO
import com.bookworm.application.customers.adapter.api.dto.{CustomerRegistrationRequestDto, ValidationErrorDto}
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.json4s.jackson.jsonOf

import javax.inject.Inject

class CustomerRegistrationRestApi @Inject() () extends Http4sDsl[IO] {

  implicit private val customerRegistrationRequestDtoEntityDecoder: EntityDecoder[IO, CustomerRegistrationRequestDto] =
    jsonOf[IO, CustomerRegistrationRequestDto]

  def routes: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "customers" / "registration" =>
        req.as[CustomerRegistrationRequestDto].flatMap { customerRegistrationRequestDto =>
          customerRegistrationRequestDto.toDomainCommandObject.fold(
            validationError => BadRequest(ValidationErrorDto.fromDomain(validationError)),
            initiateCustomerRegistrationCommand =>
          )
        }
    }
  }

}
