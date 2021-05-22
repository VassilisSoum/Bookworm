package com.bookworm.application.customers.adapter.api

import cats.effect.IO
import com.bookworm.application.customers.adapter.api.dto.{AuthenticationTokenResponseDto, CustomerAuthenticationRequestDto, ValidationErrorDto}
import com.bookworm.application.customers.adapter.service.CustomerAuthenticationApplicationService
import com.bookworm.application.customers.adapter.service.model.AuthenticationTerminationServiceModel
import com.bookworm.application.customers.domain.model.CustomerId
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.json4s.jackson.{jsonEncoderOf, jsonOf}

import javax.inject.Inject

class CustomerAuthenticationRestApi @Inject() (
    customerAuthenticationApplicationService: CustomerAuthenticationApplicationService
) extends Http4sDsl[IO] {

  implicit private val customerAuthenticationRequestDtoEntityDecoder
    : EntityDecoder[IO, CustomerAuthenticationRequestDto] = jsonOf[IO, CustomerAuthenticationRequestDto]

  implicit private val authenticationTokenResponseDtoEntityEncoder: EntityEncoder[IO, AuthenticationTokenResponseDto] =
    jsonEncoderOf[IO, AuthenticationTokenResponseDto]

  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "customers" / "authentication" =>
        req.as[CustomerAuthenticationRequestDto].flatMap { customerAuthenticationRequestDto =>
          customerAuthenticationRequestDto.toServiceModel.fold(
            validationError => BadRequest(ValidationErrorDto.fromDomain(validationError)),
            authenticationCustomerServiceModel =>
              customerAuthenticationApplicationService.login(authenticationCustomerServiceModel).flatMap {
                case Some(authenticationToken) =>
                  Ok(
                    AuthenticationTokenResponseDto(
                      accessToken = authenticationToken.token,
                      expiresIn = authenticationToken.expirationInSeconds
                    )
                  )
                case None =>
                  IO.pure(Response[IO](status = Status.Unauthorized))
              }
          )
        }

      case POST -> Root / "customers" / UUIDVar(customerId) / "authentication" / "logout" =>
        customerAuthenticationApplicationService
          .logout(AuthenticationTerminationServiceModel(CustomerId(customerId)))
          .flatMap(_ => NoContent())
    }
}
