package com.bookworm.application.customers.adapter.api.dto

import com.bookworm.application.customers.adapter.api.formats
import com.bookworm.application.customers.domain.model.DomainValidationError
import com.bookworm.application.customers.domain.model.DomainValidationError._
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, _}

case class ValidationErrorDto(errorType: DomainValidationError, message: String)

object ValidationErrorDto {

  final case object ValidationErrorSerializer
    extends CustomSerializer[DomainValidationError](_ =>
      (
        { case JString(validationError) =>
          validationError match {
            case "InvalidCustomerFirstName" => InvalidCustomerFirstName
            case "InvalidCustomerLastName"  => InvalidCustomerLastName
            case "InvalidCustomerEmail"     => InvalidCustomerEmail
            case "InvalidCustomerAge"       => InvalidCustomerAge
            case "InvalidCustomerPassword"  => InvalidCustomerPassword
          }
        },
        { case validationError: DomainValidationError =>
          JString(validationError.toString)
        }
      )
    )

  implicit val validationErrorDtoWriter: JsonFormat[ValidationErrorDto] = new JsonFormat[ValidationErrorDto] {

    override def write(validationErrorDto: ValidationErrorDto): JValue =
      Extraction.decompose(validationErrorDto)

    override def read(value: JValue): ValidationErrorDto =
      value.extract[ValidationErrorDto]
  }

  def fromDomain(validationError: DomainValidationError): ValidationErrorDto =
    validationError match {
      case InvalidCustomerFirstName =>
        ValidationErrorDto(DomainValidationError.InvalidCustomerFirstName, "Customer first name cannot be empty")
      case InvalidCustomerLastName =>
        ValidationErrorDto(DomainValidationError.InvalidCustomerLastName, "Customer last name cannot be empty")
      case InvalidCustomerEmail =>
        ValidationErrorDto(DomainValidationError.InvalidCustomerEmail, "Customer email address is invalid")
      case InvalidCustomerAge =>
        ValidationErrorDto(DomainValidationError.InvalidCustomerAge, "Customer is not an adult")
      case InvalidCustomerPassword =>
        ValidationErrorDto(DomainValidationError.InvalidCustomerPassword, "Password does not meet security constraints")
    }
}
