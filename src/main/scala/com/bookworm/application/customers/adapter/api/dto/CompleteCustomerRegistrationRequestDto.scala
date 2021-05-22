package com.bookworm.application.customers.adapter.api.dto

import com.bookworm.application.customers.adapter.api.formats
import com.bookworm.application.customers.adapter.service.model.CustomerCompetionRegistrationServiceModel
import com.bookworm.application.customers.domain.model.VerificationToken
import org.json4s.{Extraction, JValue, JsonFormat}

import java.util.UUID

case class CompleteCustomerRegistrationRequestDto(verificationToken: UUID) {

  def toServiceModel: CustomerCompetionRegistrationServiceModel =
    CustomerCompetionRegistrationServiceModel(VerificationToken(verificationToken))
}

object CompleteCustomerRegistrationRequestDto {

  implicit val completeCustomerRegistrationRequestDtoJsonFormat: JsonFormat[CompleteCustomerRegistrationRequestDto] =
    new JsonFormat[CompleteCustomerRegistrationRequestDto] {

      override def read(value: JValue): CompleteCustomerRegistrationRequestDto =
        value.extract[CompleteCustomerRegistrationRequestDto]

      override def write(completeCustomerRegistrationRequestDto: CompleteCustomerRegistrationRequestDto): JValue =
        Extraction.decompose(completeCustomerRegistrationRequestDto)
    }
}
