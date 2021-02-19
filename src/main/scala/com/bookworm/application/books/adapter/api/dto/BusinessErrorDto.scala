package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.adapter.api.formats
import com.bookworm.application.books.domain.model.BusinessError
import com.bookworm.application.books.domain.model.BusinessError.{BookDoesNotExist, OneOrMoreAuthorsDoNotExist}
import org.json4s.JsonAST.JString
import org.json4s.{CustomSerializer, Extraction, JValue, JsonFormat}

case class BusinessErrorDto(errorType: BusinessError, message: String)

object BusinessErrorDto {

  final case object BusinessErrorSerializer
    extends CustomSerializer[BusinessError](_ =>
      (
        { case JString(businessError) =>
          businessError match {
            case "OneOrMoreAuthorsDoNotExist" => BusinessError.OneOrMoreAuthorsDoNotExist
            case "BookDoesNotExist"           => BusinessError.BookDoesNotExist
          }
        },
        { case businessError: BusinessError =>
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

  def fromDomain(businessError: BusinessError): BusinessErrorDto =
    businessError match {
      case OneOrMoreAuthorsDoNotExist =>
        BusinessErrorDto(BusinessError.OneOrMoreAuthorsDoNotExist, "Book contains one or more unknown authors")
      case BookDoesNotExist =>
        BusinessErrorDto(BusinessError.BookDoesNotExist, "Book does not exist")
    }
}
