package com.bookworm.application.customers.adapter.service.model

import com.bookworm.application.customers.domain.model.{CustomerEmail, CustomerPassword}

case class AuthenticationCustomerServiceModel(email: CustomerEmail, password: CustomerPassword)
