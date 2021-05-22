package com.bookworm.application.customers.domain.model

import pdi.jwt.JwtAlgorithm

case class AuthenticationTokenConfiguration(
    jwtTokenSecretKeyEncryption: String,
    tokenExpirationInSeconds: Int,
    algorithm: JwtAlgorithm = JwtAlgorithm.HS256
)
