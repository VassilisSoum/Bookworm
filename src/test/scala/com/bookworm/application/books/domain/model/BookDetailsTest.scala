package com.bookworm.application.books.domain.model

import com.bookworm.application.AbstractUnitTest

class BookDetailsTest extends AbstractUnitTest {

  "BookDetails" should {
    "return ValidationError#MaxPriceLessThanMinPrice if max price is less than min price" in {
      val bookDetailsEither = BookDetails.create(
        title = testBookTitle,
        summary = testBookSummary,
        isbn = testBookIsbn,
        genre = testBookGenreId,
        authors = testBookAuthors,
        minPrice = testBookMinPrice,
        maxPrice = BookPrice.create(testBookMinPrice.value - 1L).toOption.get
      )
      bookDetailsEither shouldBe Left(DomainValidationError.MaxPriceLessThanMinPrice)
    }
  }

}
