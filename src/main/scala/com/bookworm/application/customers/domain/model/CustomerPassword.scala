package com.bookworm.application.customers.domain.model

import java.util.regex.Pattern

sealed abstract case class CustomerPassword private[CustomerPassword] (value: String) {

  //to ensure validation and possibly singleton-ness, we override readResolve to use explicit companion object factory method
  private def readResolve(): Object = CustomerPassword.create(value)
}

object CustomerPassword {

  private val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$"
  private val pattern = Pattern.compile(passwordPattern)

  def create(password: String): Either[DomainValidationError, CustomerPassword] = {
    val matcher = pattern.matcher(password)
    if (!matcher.matches()) Left(DomainValidationError.InvalidCustomerPassword)
    else
      Right(new CustomerPassword(password) {})
  }
}
