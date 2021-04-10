package com.bookworm.application.customers.adapter.repository.dao

import com.bookworm.application.customers.domain.model.{CustomerId, CustomerVerificationToken, VerificationToken}
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.implicits.javatime._
import doobie.postgres.implicits._

import java.sql.Timestamp
import java.time.{Clock, LocalDateTime}
import javax.inject.Inject

class CustomerVerificationTokenDao @Inject() (clock: Clock) {

  def insert(customerVerificationToken: CustomerVerificationToken): doobie.ConnectionIO[Unit] = {
    val currentTimestamp = Timestamp.valueOf(LocalDateTime.now(clock))

    sql"""
        INSERT INTO BOOKWORM.CUSTOMER_VERIFICATION_TOKEN(token,customerId,expirationDate,createdAt) 
        VALUES(
        ${customerVerificationToken.token.value}, 
        ${customerVerificationToken.customerId.id},
        ${Timestamp.valueOf(customerVerificationToken.expirationDate)},
        $currentTimestamp
        )
      """.update.run.map(_ => ())
  }

  def deleteAll(customerId: CustomerId): doobie.ConnectionIO[Unit] =
    fr"DELETE FROM BOOKWORM.CUSTOMER_VERIFICATION_TOKEN WHERE customerId=${customerId.id}".update.run
      .map(_ => ())

  def getOptionalCustomerVerificationTokenBy(
    verificationToken: VerificationToken
  ): doobie.ConnectionIO[Option[CustomerVerificationToken]] =
    fr"""SELECT
        C.token,C.customerId,C.expirationDate
        FROM BOOKWORM.CUSTOMER_VERIFICATION_TOKEN C
        WHERE C.token = ${verificationToken.value}"""
      .query[CustomerVerificationToken]
      .option

  def getAllCustomerVerificationTokensByCustomerId(
    customerId: CustomerId
  ): doobie.ConnectionIO[List[CustomerVerificationToken]] =
    fr"""SELECT
        C.token,C.customerId,C.expirationDate
        FROM BOOKWORM.CUSTOMER_VERIFICATION_TOKEN C
        WHERE C.customerId = ${customerId.id}"""
      .query[CustomerVerificationToken]
      .to[List]

  def deleteAllExpiredVerificationTokens(): doobie.ConnectionIO[Unit] = {
    val currentTime = Timestamp.valueOf(LocalDateTime.now(clock))
    fr"DELETE FROM BOOKWORM.CUSTOMER_VERIFICATION_TOKEN WHERE expirationDate <= $currentTime"
      .update
      .run
      .map(_ => ())
  }
}
