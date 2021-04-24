package com.bookworm.application.customers.domain.port.outbound

import com.bookworm.application.customers.domain.port.inbound.query.EmailTemplateQueryModel

trait CustomerEmailTemplateRepository[F[_]] {
  def findBy(templateName: String): F[Option[EmailTemplateQueryModel]]
}
