package com.bookworm.application.customers.adapter.repository

import com.bookworm.application.customers.adapter.repository.dao.EmailTemplateDao
import com.bookworm.application.customers.domain.port.inbound.query.EmailTemplateQueryModel
import com.bookworm.application.customers.domain.port.outbound.CustomerEmailTemplateRepository
import doobie.ConnectionIO

import javax.inject.Inject

private[repository] class CustomerEmailTemplateRepositoryImpl @Inject() (emailTemplateDao: EmailTemplateDao)
  extends CustomerEmailTemplateRepository[ConnectionIO] {

  override def findBy(templateName: String): ConnectionIO[Option[EmailTemplateQueryModel]] =
    emailTemplateDao.getOptionalEmailTemplateByName(templateName)
}
