package com.bookworm.application.integration.books

import com.bookworm.application.IntegrationTestModule
import com.bookworm.application.books.domain.model._
import doobie.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._

import java.util.UUID

trait TestData extends IntegrationTestModule {

  val testGenreId: GenreId = GenreId(UUID.randomUUID())
  val testGenreName: String = "TestGenre"
  val testBookId: BookId = BookId(UUID.randomUUID())
  val testBookTitle: BookTitle = BookTitle.create("TestBookTitle").toOption.get
  val testBookSummary: BookSummary = BookSummary.create("TestBookSummary").toOption.get
  val testBookIsbn: BookIsbn = BookIsbn.create("TestBookIsbn").toOption.get
  val testAuthorId: AuthorId = AuthorId(UUID.randomUUID())
  val testGenre: Genre = Genre(testGenreId, GenreName.create(testGenreName).toOption.get)
  val testBook: Book = Book(testBookId, BookDetails(testBookTitle, testBookSummary, testBookIsbn, testGenreId))
  val testAuthorFirstName: AuthorFirstName = AuthorFirstName.create("TestAuthorFirstName").toOption.get
  val testAuthorLastName: AuthorLastName = AuthorLastName.create("TestAuthorLastName").toOption.get
  val testAuthor: Author = Author(testAuthorId, AuthorDetails(testAuthorFirstName, testAuthorLastName))

  override def beforeAll(): Unit = {
    val transaction = for {
      _ <- insertIntoGenre(testGenre)
      _ <- insertIntoAuthor(testAuthor)
      _ <- insertIntoBook(testBook)
      _ <- insertIntoBookAuthor(testBookId, testAuthorId)
    } yield ()

    transaction.transact(this.synchronousTransactor).unsafeRunSync()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    val transaction = for {
      _ <- sql"""truncate table bookworm.author CASCADE""".update.run
      _ <- sql"""truncate table bookworm.book CASCADE""".update.run
      _ <- sql"""truncate table bookworm.book_author CASCADE""".update.run
      _ <- sql"""truncate table bookworm.genre CASCADE""".update.run
    } yield ()

    transaction.transact(this.synchronousTransactor).unsafeRunSync()
    super.afterAll()
  }

  def insertIntoGenre(genre: Genre): ConnectionIO[Int] =
    sql"""insert into bookworm.genre(genreId, genreName)
         values (${genre.genreId.id}, ${genre.genreName.genre})
         """.update.run

  def insertIntoAuthor(author: Author): ConnectionIO[Int] =
    sql"""insert into bookworm.author(authorId,firstName,lastName) 
         values (
          ${author.authorId.id},${author.authorDetails.firstName.firstName},
          ${author.authorDetails.lastName.lastName}
         )""".update.run

  def insertIntoBook(book: Book): ConnectionIO[Int] =
    sql"""insert into bookworm.book(bookId,title,summary,isbn,genreId) 
         values (
          ${book.bookId.id},
          ${book.bookDetails.title.title},
          ${book.bookDetails.summary.summary},
          ${book.bookDetails.isbn.isbn},
          ${book.bookDetails.genre.id}
         )""".update.run

  def insertIntoBookAuthor(bookId: BookId, authorId: AuthorId): ConnectionIO[Int] =
    sql"""insert into bookworm.book_author(bookId,authorId) 
         values (${bookId.id},${authorId.id})
         """.update.run
}
