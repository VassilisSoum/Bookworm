package com.bookworm.application.customers.adapter.api

import cats.effect.IO
import com.bookworm.application.customers.adapter.api.dto.{BusinessErrorDto, CompleteCustomerRegistrationRequestDto, CustomerRegistrationRequestDto, ValidationErrorDto}
import com.bookworm.application.customers.adapter.service.CustomerApplicationService
import org.http4s.dsl.Http4sDsl
import org.http4s.json4s.jackson.jsonOf
import org.http4s.{EntityDecoder, HttpRoutes}

import javax.inject.Inject

class CustomerRegistrationRestApi @Inject() (customerApplicationService: CustomerApplicationService)
  extends Http4sDsl[IO] {

  implicit private val customerRegistrationRequestDtoEntityDecoder: EntityDecoder[IO, CustomerRegistrationRequestDto] =
    jsonOf[IO, CustomerRegistrationRequestDto]

  implicit private val completeCustomerRegistrationRequestDtoEntityDecoder
    : EntityDecoder[IO, CompleteCustomerRegistrationRequestDto] =
    jsonOf[IO, CompleteCustomerRegistrationRequestDto]

  //TODO: Add endpoint for retrieving a customer by customer id
  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "customers" / "registration" =>
        req.as[CustomerRegistrationRequestDto].flatMap { customerRegistrationRequestDto =>
          customerRegistrationRequestDto.toServiceModel.fold(
            validationError => BadRequest(ValidationErrorDto.fromDomain(validationError)),
            initiateCustomerRegistrationServiceModel =>
              customerApplicationService
                .initiateCustomerRegistration(initiateCustomerRegistrationServiceModel)
                .flatMap {
                  case Left(businessError) => Conflict(BusinessErrorDto.fromDomain(businessError))
                  case Right(_)            => NoContent()
                }
          )
        }
      case req @ POST -> Root / "customers" / "complete-registration" =>
        req.as[CompleteCustomerRegistrationRequestDto].flatMap { completeCustomerRegistrationRequestDto =>
          customerApplicationService
            .completeCustomerRegistration(completeCustomerRegistrationRequestDto.toServiceModel)
            .flatMap {
              case Left(businessError) => Conflict(BusinessErrorDto.fromDomain(businessError))
              case Right(_)            => NoContent()
            }
        }
    }

}
