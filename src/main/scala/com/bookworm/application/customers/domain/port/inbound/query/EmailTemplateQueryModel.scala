package com.bookworm.application.customers.domain.port.inbound.query

case class EmailTemplateQueryModel(
    templateName: String,
    templateSubject: String,
    templateBody: String
)
