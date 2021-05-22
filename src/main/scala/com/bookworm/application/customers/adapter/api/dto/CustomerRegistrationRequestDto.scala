package com.bookworm.application.customers.adapter.api.dto

import com.bookworm.application.customers.adapter.api.formats
import com.bookworm.application.customers.adapter.service.model.CustomerInitiationRegistrationServiceModel
import com.bookworm.application.customers.domain.model._
import org.json4s.{Extraction, JValue, JsonFormat}

import java.util.UUID

case class CustomerRegistrationRequestDto(
    firstName: String,
    lastName: String,
    email: String,
    age: Int,
    password: String
) {

  def toServiceModel: Either[DomainValidationError, CustomerInitiationRegistrationServiceModel] =
    for {
      customerFirstName <- CustomerFirstName.create(firstName)
      customerLastName <- CustomerLastName.create(lastName)
      customerEmail <- CustomerEmail.create(email)
      customerAge <- CustomerAge.create(age)
      customerPassword <- CustomerPassword.create(password)
      customerId = CustomerId(UUID.randomUUID())
    } yield CustomerInitiationRegistrationServiceModel(
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
