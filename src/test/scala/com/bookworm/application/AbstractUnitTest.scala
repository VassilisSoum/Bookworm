package com.bookworm.application

import com.bookworm.application.books.domain.model._
import com.bookworm.application.books.domain.port.inbound.query.{AuthorQueryModel, BookQueryModel}
import com.bookworm.application.customers.domain.model._
import com.bookworm.application.customers.domain.port.inbound.query.CustomerQueryModel
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import java.time.{Clock, Instant, LocalDateTime, ZoneId}
import java.util.UUID

abstract class AbstractUnitTest extends WordSpec with Matchers with MockFactory {

  val testBookTitle = BookTitle.create("title").toOption.get
  val testBookSummary = BookSummary.create("summary").toOption.get
  val testBookGenreId = GenreId(UUID.randomUUID())
  val testBookGenreName = GenreName.create("Genre").toOption.get
  val testBookGenre = Genre(testBookGenreId, testBookGenreName)
  val testBookAuthors = List(AuthorId(UUID.randomUUID()))
  val testBookIsbn = BookIsbn.create("9781234567897").toOption.get
  val testBookMinPrice = BookPrice.create(1000L).toOption.get
  val testBookMaxPrice = BookPrice.create(5000L).toOption.get

  val testBook: Book = Book(
    bookId = BookId(UUID.randomUUID()),
    bookDetails = BookDetails
      .create(
        title = testBookTitle,
        summary = testBookSummary,
        isbn = testBookIsbn,
        genre = testBookGenreId,
        authors = testBookAuthors,
        minPrice = testBookMinPrice,
        maxPrice = testBookMaxPrice
      )
      .toOption
      .get,
    bookStatus = BookStatus.Available
  )

  val testBookQueryModel = BookQueryModel(
    bookId = testBook.bookId.id,
    title = testBookTitle.value,
    summary = testBookSummary.value,
    isbn = testBookIsbn.value,
    genre = testBookGenreName.genre,
    minPrice = testBookMinPrice.value,
    maxPrice = testBookMaxPrice.value,
    updatedAt = LocalDateTime.now(),
    id = 1L
  )

  val testAuthorId = AuthorId(UUID.randomUUID())
  val testAuthorFirstName = AuthorFirstName.create("Bill").toOption.get
  val testAuthorLastName = AuthorLastName.create("Soumakis").toOption.get

  val testAuthorQueryModel =
    AuthorQueryModel(
      authorId = testAuthorId.id,
      firstName = testAuthorFirstName.firstName,
      lastName = testAuthorLastName.lastName
    )

  val customerId = CustomerId(UUID.randomUUID())
  val customerFirstName = CustomerFirstName.create("Bill").toOption.get
  val customerLastName = CustomerLastName.create("Soumakis").toOption.get
  val customerEmail = CustomerEmail.create("someone@test.com").toOption.get
  val customerAge = CustomerAge.create(28).toOption.get
  val customerPassword = CustomerPassword.create("Someone@123").toOption.get

  val pendingCustomer = Customer(
    customerId = customerId,
    customerDetails = CustomerDetails(customerFirstName, customerLastName, customerEmail, customerAge),
    customerPassword = customerPassword,
    customerRegistrationStatus = CustomerRegistrationStatus.Pending
  )

  val registeredCustomer = Customer(
    customerId = customerId,
    customerDetails = CustomerDetails(customerFirstName, customerLastName, customerEmail, customerAge),
    customerPassword = customerPassword,
    customerRegistrationStatus = CustomerRegistrationStatus.Completed
  )

  val expiredRegistrationCustomer = Customer(
    customerId = customerId,
    customerDetails = CustomerDetails(customerFirstName, customerLastName, customerEmail, customerAge),
    customerPassword = customerPassword,
    customerRegistrationStatus = CustomerRegistrationStatus.Expired
  )

  val verificationToken = VerificationToken(UUID.randomUUID())

  val pendingCustomerQueryModel = CustomerQueryModel(
    customerId = pendingCustomer.customerId.id,
    customerFirstName = pendingCustomer.customerDetails.customerFirstName.value,
    customerLastName = pendingCustomer.customerDetails.customerLastName.value,
    customerEmail = pendingCustomer.customerDetails.customerEmail.value,
    customerAge = pendingCustomer.customerDetails.customerAge.value,
    customerRegistrationStatus = CustomerRegistrationStatus.Pending
  )

  val expiredRegistrationCustomerQueryModel = CustomerQueryModel(
    customerId = expiredRegistrationCustomer.customerId.id,
    customerFirstName = expiredRegistrationCustomer.customerDetails.customerFirstName.value,
    customerLastName = expiredRegistrationCustomer.customerDetails.customerLastName.value,
    customerEmail = expiredRegistrationCustomer.customerDetails.customerEmail.value,
    customerAge = expiredRegistrationCustomer.customerDetails.customerAge.value,
    customerRegistrationStatus = CustomerRegistrationStatus.Expired
  )

  val registeredCustomerQueryModel = CustomerQueryModel(
    customerId = registeredCustomer.customerId.id,
    customerFirstName = registeredCustomer.customerDetails.customerFirstName.value,
    customerLastName = registeredCustomer.customerDetails.customerLastName.value,
    customerEmail = registeredCustomer.customerDetails.customerEmail.value,
    customerAge = registeredCustomer.customerDetails.customerAge.value,
    customerRegistrationStatus = CustomerRegistrationStatus.Completed
  )

  val customerVerificationToken =
    CustomerVerificationToken(
      token = verificationToken,
      customerId = customerId,
      expirationDate = LocalDateTime.of(2025, 12, 12, 10, 0, 0)
    )

  val fixedClock = Clock.fixed(Instant.parse("2021-03-24T00:00:00Z"), ZoneId.of("UTC"))
}
