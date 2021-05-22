package com.bookworm.application.customers.adapter.api.dto

import com.bookworm.application.customers.adapter.api.formats
import com.bookworm.application.customers.adapter.service.model.AuthenticationCustomerServiceModel
import com.bookworm.application.customers.domain.model.{CustomerEmail, CustomerPassword, DomainValidationError}
import org.json4s.{Extraction, JValue, JsonFormat}

case class CustomerAuthenticationRequestDto(email: String, password: String) {

  def toServiceModel: Either[DomainValidationError, AuthenticationCustomerServiceModel] =
    for {
      customerEmail <- CustomerEmail.create(email)
      customerPassword <- CustomerPassword.create(password)
    } yield AuthenticationCustomerServiceModel(customerEmail, customerPassword)
}

object CustomerAuthenticationRequestDto {

  implicit val customerAuthenticationRequestDtoJsonFormat: JsonFormat[CustomerAuthenticationRequestDto] =
    new JsonFormat[CustomerAuthenticationRequestDto] {

      override def read(value: JValue): CustomerAuthenticationRequestDto =
        value.extract[CustomerAuthenticationRequestDto]

      override def write(customerAuthenticationRequestDto: CustomerAuthenticationRequestDto): JValue =
        Extraction.decompose(customerAuthenticationRequestDto)
    }
}
