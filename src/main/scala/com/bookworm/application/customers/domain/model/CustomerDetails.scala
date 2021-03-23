package com.bookworm.application.customers.domain.model

import org.apache.commons.validator.routines.EmailValidator

sealed abstract case class CustomerFirstName private[CustomerFirstName] (value: String) {

  //to ensure validation and possibly singleton-ness, we override readResolve to use explicit companion object factory method
  private def readResolve(): Object = CustomerFirstName.create(value)
}

object CustomerFirstName {

  def create(firstName: String): Either[DomainValidationError, CustomerFirstName] =
    if (firstName.isEmpty) Left(DomainValidationError.InvalidCustomerFirstName)
    else
      Right(new CustomerFirstName(firstName) {})
}

sealed abstract case class CustomerLastName private[CustomerLastName] (value: String) {

  //to ensure validation and possibly singleton-ness, we override readResolve to use explicit companion object factory method
  private def readResolve(): Object = CustomerLastName.create(value)
}

object CustomerLastName {

  def create(lastName: String): Either[DomainValidationError, CustomerLastName] =
    if (lastName.isEmpty) Left(DomainValidationError.InvalidCustomerLastName)
    else
      Right(new CustomerLastName(lastName) {})
}

sealed abstract case class CustomerEmail private[CustomerEmail] (value: String) {

  //to ensure validation and possibly singleton-ness, we override readResolve to use explicit companion object factory method
  private def readResolve(): Object = CustomerEmail.create(value)
}

object CustomerEmail {
  private val emailValidator = EmailValidator.getInstance(false)

  def create(email: String): Either[DomainValidationError, CustomerEmail] =
    if (!emailValidator.isValid(email)) Left(DomainValidationError.InvalidCustomerEmail)
    else
      Right(new CustomerEmail(email) {})
}

sealed abstract case class CustomerAge private[CustomerAge] (value: Int) {

  //to ensure validation and possibly singleton-ness, we override readResolve to use explicit companion object factory method
  private def readResolve(): Object = CustomerAge.create(value)
}

object CustomerAge {

  private val minimumAllowedAge = 18

  def create(age: Int): Either[DomainValidationError, CustomerAge] =
    if (age < minimumAllowedAge) Left(DomainValidationError.InvalidCustomerAge)
    else
      Right(new CustomerAge(age) {})
}

case class CustomerDetails(
    customerFirstName: CustomerFirstName,
    customerLastName: CustomerLastName,
    customerEmail: CustomerEmail,
    customerAge: CustomerAge
)
