package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.adapter.api.formats
import com.bookworm.application.books.domain.port.inbound.query.AuthorQueryModel
import org.json4s.{Extraction, JValue, JsonFormat}

case class AuthorResponseDto(authorId: String, firstName: String, lastName: String)

object AuthorResponseDto {

  implicit val authorResponseDtoFormat: JsonFormat[AuthorResponseDto] = new JsonFormat[AuthorResponseDto] {

    override def write(authorResponseDto: AuthorResponseDto): JValue =
      Extraction.decompose(authorResponseDto)

    override def read(value: JValue): AuthorResponseDto =
      value.extract[AuthorResponseDto]
  }

  implicit class AuthorResponseDtoOps(authorQueryModel: AuthorQueryModel) {

    def fromDomainQueryModel: AuthorResponseDto =
      AuthorResponseDto(
        authorId = authorQueryModel.authorId.toString,
        firstName = authorQueryModel.firstName,
        lastName = authorQueryModel.lastName
      )
  }
}
