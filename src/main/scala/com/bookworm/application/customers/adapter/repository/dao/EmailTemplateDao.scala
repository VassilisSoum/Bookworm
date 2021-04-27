package com.bookworm.application.customers.adapter.repository.dao

import com.bookworm.application.customers.domain.port.inbound.query.EmailTemplateQueryModel
import doobie.ConnectionIO
import doobie.implicits._

import javax.inject.Inject

class EmailTemplateDao @Inject() () {

  def getOptionalEmailTemplateByName(templateName: String): ConnectionIO[Option[EmailTemplateQueryModel]] =
    fr"SELECT * FROM BOOKWORM.EMAIL_TEMPLATE et WHERE et.name = $templateName"
      .query[EmailTemplateQueryModel]
      .option

  def insertEmailTemplate(templateName: String, templateSubject: String, templateBody: String): ConnectionIO[Unit] =
    fr"INSERT INTO BOOKWORM.EMAIL_TEMPLATE(name,subject,body) VALUES ($templateName,$templateSubject,$templateBody)"
      .update
      .run
      .map(_ => ())
}
