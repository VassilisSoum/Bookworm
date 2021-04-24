package com.bookworm.application.customers.domain.port.inbound

import cats.Monad
import com.bookworm.application.customers.domain.model.CustomerId
import com.bookworm.application.customers.domain.port.inbound.query.CustomerQueryModel
import com.bookworm.application.customers.domain.port.outbound.CustomerRepository

import javax.inject.Inject

class RetrieveCustomerDetailsUseCase[F[_]: Monad] @Inject() (customerRepository: CustomerRepository[F]) {

  def retrieveCustomerDetails(customerId: CustomerId): F[Option[CustomerQueryModel]] =
    customerRepository.findBy(customerId)
}
