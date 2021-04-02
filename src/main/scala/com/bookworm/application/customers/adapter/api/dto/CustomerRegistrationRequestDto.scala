package com.bookworm.application.customers.adapter.api.dto

import com.bookworm.application.customers.adapter.api.formats
import com.bookworm.application.customers.domain.model._
import com.bookworm.application.customers.domain.port.inbound.command.InitiateCustomerRegistrationCommand
import org.json4s.{Extraction, JValue, JsonFormat}

import java.util.UUID

case class CustomerRegistrationRequestDto(
    firstName: String,
    lastName: String,
    email: String,
    age: Int,
    password: String
) {

  def toDomainCommandObject: Either[DomainValidationError, InitiateCustomerRegistrationCommand] =
    for {
      customerFirstName <- CustomerFirstName.create(firstName)
      customerLastName <- CustomerLastName.create(lastName)
      customerEmail <- CustomerEmail.create(email)
      customerAge <- CustomerAge.create(age)
      customerPassword <- CustomerPassword.create(password)
      customerId = CustomerId(UUID.randomUUID())
    } yield InitiateCustomerRegistrationCommand(
      customerId,
      customerFirstName,
      customerLastName,
      customerEmail,
      customerAge,
      customerPassword
    )
}

object CustomerRegistrationRequestDto {

  implicit val customerRegistrationRequestDtoJsonFormat: JsonFormat[CustomerRegistrationRequestDto] =
    new JsonFormat[CustomerRegistrationRequestDto] {

      override def read(value: JValue): CustomerRegistrationRequestDto =
        value.extract[CustomerRegistrationRequestDto]

      override def write(customerRegistrationRequestDto: CustomerRegistrationRequestDto): JValue =
        Extraction.decompose(customerRegistrationRequestDto)
    }
}
