package com.bookworm.application.customers.adapter.api.dto

import com.bookworm.application.customers.adapter.api.formats
import com.bookworm.application.customers.domain.model.DomainBusinessError
import com.bookworm.application.customers.domain.model.DomainBusinessError._
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, Extraction, JValue, JsonFormat}

case class BusinessErrorDto(errorType: DomainBusinessError, message: String)

object BusinessErrorDto {

  final case object BusinessErrorSerializer
    extends CustomSerializer[DomainBusinessError](_ =>
      (
        { case JString(businessError) =>
          businessError match {
            case "CustomerDoesNotExists"          => CustomerDoesNotExists
            case "VerificationTokenExpired"       => VerificationTokenExpired
            case "CustomerAlreadyExists"          => CustomerAlreadyExists
            case "CustomerAlreadyRegistered"      => CustomerAlreadyRegistered
            case "VerificationTokenDoesNotExists" => VerificationTokenDoesNotExists
          }
        },
        { case businessError: DomainBusinessError =>
          JString(businessError.toString)
        }
      )
    )

  implicit val businessErrorDtoWriter: JsonFormat[BusinessErrorDto] = new JsonFormat[BusinessErrorDto] {

    override def write(businessErrorDto: BusinessErrorDto): JValue =
      Extraction.decompose(businessErrorDto)

    override def read(value: JValue): BusinessErrorDto =
      value.extract[BusinessErrorDto]
  }

  def fromDomain(businessError: DomainBusinessError): BusinessErrorDto =
    businessError match {
      case CustomerDoesNotExists =>
        BusinessErrorDto(CustomerDoesNotExists, "Customer does not exist")
      case VerificationTokenExpired =>
        BusinessErrorDto(VerificationTokenExpired, "Verification token has already expired")
      case CustomerAlreadyExists =>
        BusinessErrorDto(CustomerAlreadyExists, "Customer already exists")
      case CustomerAlreadyRegistered =>
        BusinessErrorDto(CustomerAlreadyRegistered, "Customer is already registered")
      case VerificationTokenDoesNotExists =>
        BusinessErrorDto(VerificationTokenDoesNotExists, "Verification token for registration does not exists")
    }
}
