package com.bookworm.application.books.adapter.api.dto

import com.bookworm.application.books.adapter.api.formats
import org.json4s.{Extraction, JValue, JsonFormat}

case class GetAuthorsByBookIdResponseDto(authors: List[AuthorResponseDto])

object GetAuthorsByBookIdResponseDto {

  implicit val getAuthorsByBookIdResponseDtoFormat: JsonFormat[GetAuthorsByBookIdResponseDto] =
    new JsonFormat[GetAuthorsByBookIdResponseDto] {

      override def write(getAuthorsByBookIdResponseDto: GetAuthorsByBookIdResponseDto): JValue =
        Extraction.decompose(getAuthorsByBookIdResponseDto)

      override def read(value: JValue): GetAuthorsByBookIdResponseDto =
        value.extract[GetAuthorsByBookIdResponseDto]
    }
}
