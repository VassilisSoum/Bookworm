package com.bookworm.application.customers.adapter.repository.dao

import com.bookworm.application.customers.adapter.repository.dao.CustomerDao.encryptionAlgorithm
import com.bookworm.application.customers.domain.model.{Customer, CustomerEmail, CustomerId, CustomerRegistrationStatus}
import com.bookworm.application.customers.domain.port.inbound.query.CustomerQueryModel
import doobie.Get
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres.implicits._

import java.sql.Timestamp
import java.time.{Clock, LocalDateTime}
import javax.inject.Inject
import scala.annotation.nowarn

class CustomerDao @Inject() (clock: Clock) {

  @nowarn implicit private val customerRegistrationStatusGet: Get[CustomerRegistrationStatus] =
    Get[String].tmap(CustomerRegistrationStatus.fromRegistrationStatus)

  def exists(customerEmail: CustomerEmail): doobie.ConnectionIO[Boolean] =
    fr"SELECT COUNT(*) FROM BOOKWORM.CUSTOMER C WHERE C.username = ${customerEmail.value}"
      .query[Long]
      .unique
      .map(_ > 0)

  def getOptionalByCustomerId(customerId: CustomerId): doobie.ConnectionIO[Option[CustomerQueryModel]] =
    fr"""SELECT C.id,C.firstName,C.lastName,C.username,C.age,C.registrationStatus FROM BOOKWORM.CUSTOMER C
          WHERE C.id = ${customerId.id}"""
      .query[CustomerQueryModel]
      .option

  def insert(customer: Customer): doobie.ConnectionIO[Unit] = {
    val currentTimestamp = Timestamp.valueOf(LocalDateTime.now(clock))

    sql"""
        INSERT INTO BOOKWORM.CUSTOMER(id,username,password,firstName,lastName,age,registrationStatus,createdAt,updatedAt)
        VALUES (
        ${customer.customerId.id}, 
        ${customer.customerDetails.customerEmail.value},
        crypt(${customer.customerPassword.value}, gen_salt($encryptionAlgorithm)),
        ${customer.customerDetails.customerFirstName.value},
        ${customer.customerDetails.customerLastName.value},
        ${customer.customerDetails.customerAge.value},
        ${customer.customerRegistrationStatus.toString},
        $currentTimestamp,
        $currentTimestamp
        )
      """.update.run.map(_ => ())
  }

  def updateRegistrationStatus(
    customerId: CustomerId,
    customerRegistrationStatus: CustomerRegistrationStatus
  ): doobie.ConnectionIO[Unit] = {
    val currentTimestamp = Timestamp.valueOf(LocalDateTime.now(clock))

    fr"""
       UPDATE BOOKWORM.CUSTOMER 
       SET 
        registrationStatus=${customerRegistrationStatus.toString},
        updatedAt=$currentTimestamp 
        
       WHERE id=${customerId.id}
      """.update.run.map(_ => ())
  }
}

object CustomerDao {
  private val encryptionAlgorithm = "bf" //Blowfish algorithm
}
