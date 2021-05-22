package com.bookworm.application.customers.adapter.api.dto

import com.bookworm.application.customers.adapter.api.formats
import org.json4s.{Extraction, JValue, JsonFormat}

case class AuthenticationTokenResponseDto(accessToken: String, tokenType: String = "Bearer", expiresIn: Int)

object AuthenticationTokenResponseDto {

  implicit val authenticationTokenResponseDtoJsonFormat: JsonFormat[AuthenticationTokenResponseDto] =
    new JsonFormat[AuthenticationTokenResponseDto] {

      override def read(value: JValue): AuthenticationTokenResponseDto =
        value.extract[AuthenticationTokenResponseDto]

      override def write(authenticationTokenResponseDto: AuthenticationTokenResponseDto): JValue =
        Extraction.decompose(authenticationTokenResponseDto)
    }
}
