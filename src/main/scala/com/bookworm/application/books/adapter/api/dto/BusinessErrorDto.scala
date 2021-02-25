package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.adapter.api.formats
import com.bookworm.application.books.domain.model.DomainBusinessError
import com.bookworm.application.books.domain.model.DomainBusinessError.{BookDoesNotExist, OneOrMoreAuthorsDoNotExist}
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, Extraction, JValue, JsonFormat}

case class BusinessErrorDto(errorType: DomainBusinessError, message: String)

object BusinessErrorDto {

  final case object BusinessErrorSerializer
    extends CustomSerializer[DomainBusinessError](_ =>
      (
        { case JString(businessError) =>
          businessError match {
            case "OneOrMoreAuthorsDoNotExist" => DomainBusinessError.OneOrMoreAuthorsDoNotExist
            case "BookDoesNotExist"           => DomainBusinessError.BookDoesNotExist
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
      case OneOrMoreAuthorsDoNotExist =>
        BusinessErrorDto(DomainBusinessError.OneOrMoreAuthorsDoNotExist, "Book contains one or more unknown authors")
      case BookDoesNotExist =>
        BusinessErrorDto(DomainBusinessError.BookDoesNotExist, "Book does not exist")
    }
}
