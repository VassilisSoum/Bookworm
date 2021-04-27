package com.bookworm.application.customers.adapter.service

import com.amazonaws.services.simpleemail.model.{SendEmailRequest, SendEmailResult}
import com.bookworm.application.customers.adapter.repository.dao.EmailTemplateDao
import com.bookworm.application.customers.adapter.service.model.SendEmailVerificationServiceModel
import com.bookworm.application.integration.customers.TestData

import java.util.UUID
import scala.util.Success

class CustomerRegistrationVerificationEmailProducerServiceTest extends TestData {

  val customerRegistrationVerificationEmailProducerService: CustomerRegistrationVerificationEmailProducerService =
    injector.getInstance(classOf[CustomerRegistrationVerificationEmailProducerService])

  val emailTemplateDao: EmailTemplateDao = injector.getInstance(classOf[EmailTemplateDao])

  val sendEmailResult = new SendEmailResult()

  val templateName: String = customerRegistrationVerificationConfig.registrationVerificationEmailTemplateName
  val templateSubject: String = "subject"
  val templateBody: String = "body"

  override def beforeAll(): Unit =
    runInTransaction(emailTemplateDao.insertEmailTemplate(templateName, templateSubject, templateBody))

  "CustomerRegistrationVerificationEmailProducerService" should {
    "send a registration verification email" in {
      (amazonSimpleEmailService.sendEmail(_: SendEmailRequest)).expects(*).returns(sendEmailResult)

      val sendEmailVerificationServiceModel = SendEmailVerificationServiceModel(
        customerFirstName = testCustomerFirstName.value,
        customerLastName = testCustomerLastName.value,
        customerEmail = testCustomerEmail.value,
        verificationToken = UUID.randomUUID()
      )

      customerRegistrationVerificationEmailProducerService
        .sendRegistrationVerificationEmail(
          sendEmailVerificationServiceModel
        )
        .unsafeRunSync() shouldBe Success(())
    }
  }
}
